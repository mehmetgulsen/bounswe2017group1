from rest_framework import status
from rest_framework.response import Response

from api.models import Heritage, Profile
import datetime
from api.serializer.heritage import HeritageSerializer

from api.service import heritage

import re
import operator

TITLE_COEF = 5
DESC_COEF = 3
LOC_COEF = 3
TAG_COEF = 5
RELATED_TAG_COEF = 1

def calculate_scores(word, filters):
    scores = {}

    items = Heritage.objects.all()

    if filters is not None:
        location = filters.get('location', None)
        creator = filters.get('creator', None)
        creation_start = filters.get('creation_start', None)
        creation_end = filters.get('creation_end', None)
        event_start = filters.get('event_start', None)
        event_end = filters.get('event_end', None)

        if location is not None:
            items = items.filter(location__icontains=location)

        if creator is not None:
            items = items.filter(creator__username__iexact=creator)

        if creation_start is not None and creation_end is not None:
            items = items.filter(creation_date__range=[creation_start, creation_end])
        elif creation_start is not None:
            items = items.filter(creation_date__range=[creation_start, datetime.datetime.max])
        elif creation_end is not None:
            items = items.filter(creation_date__range=[datetime.datetime.min, creation_end])

        if event_start is not None and event_end is not None:
            items = items.filter(event_date__range=[event_start, event_end])
        elif event_start is not None:
            items = items.filter(creation_date__range=[event_start, datetime.datetime.max])
        elif event_end is not None:
            items = items.filter(creation_date__range=[datetime.datetime.min, event_end])

    for item in items:
        score = 0
        score += TITLE_COEF * int((re.search(word, item.title, re.IGNORECASE)) is not None)
        score += DESC_COEF * int((re.search(word, item.description, re.IGNORECASE)) is not None)
        score += LOC_COEF * int((re.search(word, item.location, re.IGNORECASE)) is not None)
        tags = heritage.get_all_tags(item.id)
        for tag in tags:
            found_in_name = int((re.search(word, tag['name'], re.IGNORECASE)) is not None)
            score += TAG_COEF * found_in_name
            if found_in_name==0:
                score += RELATED_TAG_COEF * len(re.findall(word, tag['related_list'], re.IGNORECASE))

        if score > 0:
            scores[item.id] = score

    #print scores
    scores = sorted(scores.items(), key=lambda x: x[1], reverse=True)
    return scores


def get_items_by_location(location):
    items = Heritage.objects.filter(location__istartswith=location)
    response_data = {}

    for item in items:
        print item


def get_items_by_tag(tag):
    items = Heritage.objects.filter(tags__name__icontains=tag)
    print items


def get_items_by_title(title):
    items = Heritage.objects.filter(title__icontains=title)
    print items


def get_items_by_creator(creator):
    items = Heritage.objects.filter(creator__user__username__istartswith=creator)
    print items