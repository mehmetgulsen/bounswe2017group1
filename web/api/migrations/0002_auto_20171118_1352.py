# -*- coding: utf-8 -*-
# Generated by Django 1.11.5 on 2017-11-18 13:52
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='heritage',
            name='tags',
            field=models.ManyToManyField(blank=True, related_name='heritage_id', to='api.Tag'),
        ),
    ]