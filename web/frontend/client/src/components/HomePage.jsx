import React, { Component } from 'react';
import { Card, CardTitle, CardHeader, CardText } from 'material-ui/Card'
import TopBar from './TopBar.jsx'
import Auth from '../../modules/Auth.js'
import { Tabs, Tab } from 'material-ui/Tabs'
import 'whatwg-fetch'
import appConstants from '../../modules/appConstants.js'
import { Button, Form, FormGroup, InputGroup, ControlLabel, FormControl } from 'react-bootstrap';

var baseUrl = appConstants.baseUrl;
const HomePage = React.createClass ({
	getInitialState: function() {
    return {
			filterText: '',
			value: 'a',
			items: [],
			hideAdvanced: true
    };
	},
	
	handleChange: function(value){
    this.setState({
      value: value,
    });
	},
	componentDidMount(){
		fetch(baseUrl+'/api/items',{
      method: "GET",
      headers: {
        "Access-Control-Allow-Origin" : "*",
        "Content-Type": "application/json"
      },
      credentials: "same-origin"
    }).then(response=>{
      if(response.ok){
        response.json().then(res=>{
          this.setState({items: res});
          console.log(res);
        });
      }
    });
	},
  handleUserInput: function(filterText, inStockOnly) {
    this.setState({
      filterText: filterText
    });
	},
	onSearch(event){
		event.preventDefault();
		console.log(this.state.filterText);
		fetch(baseUrl+'/api/search',{
      method: "POST",
      headers: {
        "Access-Control-Allow-Origin" : "*",
        "Content-Type": "application/json"
      },
			credentials: "same-origin",
			body: JSON.stringify({
				query: this.state.filterText
			})
    }).then(response=>{
      if(response.ok){
        response.json().then(res=>{
          this.setState({items: res});
          console.log(res);
        });
      }
    });
	},
	onToggle (event) {
		event.preventDefault();
		this.setState({
			hideAdvanced: !this.state.hideAdvanced
		})
	},
	render() {
		return(
			<div>
				<TopBar auth={Auth.isUserAuthenticated()}/>
				<div style={{width: '50%', margin: 'auto'}}>
					<SearchBar
						filterText={this.state.filterText}
						inStockOnly={this.state.inStockOnly}
						onUserInput={this.handleUserInput}
						onSubmit={this.onSearch}
						onToggle={this.onToggle}
					/>
					<div>
						<Form inline style={this.state.hideAdvanced? {display: 'none'}: {} }inline>
							<FormGroup style={{margin: '5px'}}>
								<ControlLabel>Location Name</ControlLabel>
								{' '}
								<FormControl type="text" placeholder="Location..." />
							</FormGroup>
							{' '}
							<FormGroup style={{margin: '5px'}}>
								<ControlLabel>Creator Name</ControlLabel>
								{' '}
								<FormControl type="text" placeholder="Creator name.." />
							</FormGroup>
							{' '}
						</Form>
					</div>
					{this.renderTabs()}
				</div>
  		</div>
		);
	},
	renderTabs(){
		return(
			<Tabs
				value={this.state.value}
				onChange={this.handleChange}
				style={{ marginTop: '20px'}}
				tabItemContainerStyle={{ backgroundColor: '#757575' }}
				inkBarStyle={{ backgroundColor: '#212121' }}
			>
				<Tab label="Random" value="a">
					{this.state.items.map((item, index)=>(
						<div style={{marginTop: '20px'}}>
							<Card style={{ backgroundColor: '#E0E0E0' }}>
								<a className="nav-link" href={'/item/'+item.id}>
								<CardHeader
									title={item.title}
									titleStyle={{fontWeight: 'bold'}}/>
								</a>
								<CardText>{ item.description} </CardText>

							</Card>
						</div>
					))}
				</Tab>
				<Tab label="Best" value="b">
				<div style={{marginTop: '20px'}}>
					{this.state.items.map((item, index)=>(
						<div style={{marginTop: '20px'}}>
							<Card style={{ backgroundColor: '#E0E0E0' }}>
								<CardHeader
									title={item.title}
									titleStyle={{fontWeight: 'bold'}}
								/>
								<CardText expandable={true}>{ item.description} </CardText>
							</Card>
						</div>
					))}
				</div>
				</Tab>
				<Tab label="Trended" value="c">
					{this.state.items.map((item, index)=>(
							<div style={{marginTop: '20px'}}>
								<Card style={{ backgroundColor: '#E0E0E0' }}>
									<CardHeader
										title={item.title}
										titleStyle={{fontWeight: 'bold'}}
									/>
									<CardText>{ item.description} </CardText>
								</Card>
							</div>
						))}
				</Tab>
				<Tab label="New" value="d">
					{this.state.items.map((item, index)=>(
							<div style={{marginTop: '20px'}}>
								<Card style={{ backgroundColor: '#E0E0E0' }}>
									<CardHeader
										title={item.title}
										titleStyle={{fontWeight: 'bold'}}
									/>
									<CardText>{ item.description} </CardText>
								</Card>
							</div>
						))}
				</Tab>
			</Tabs>
		);
	}
});


/* const HomePage = () => (	
  <div>
  	<TopBar auth={Auth.isUserAuthenticated()}/>
  	<div>
			<SearchBar/>
		</div>
  </div>
); */

const SearchBar = React.createClass({
  handleChange: function() {
    this.props.onUserInput(
      this.refs.filterTextInput.value
    );
  },
  render: function() {
    return (
      <FormGroup onSubmit={this.props.onSubmit}>
				<InputGroup>
					<input
						type="text"
						placeholder="Search..."
						value={this.props.filterText}
						ref="filterTextInput"
						onChange={this.handleChange}
						style={inputStyle}
					/>
					<input type="submit" style={{display: 'none'}}/>
					<button style={buttonStyle} onClick={this.props.onToggle}>
						<img src="http://www.pvhc.net/img6/qelyglamexpdbblhacjf.png" style={{height: '32px', width: '32px'}}/>
					</button>
				</InputGroup>
        
      </FormGroup>
    );
  }
});

const inputStyle = {
		width: 'calc(100% - 44px)',
    padding: '12px 20px',
    margin: '8px 0',
    display: 'inline-block',
    border: '2px solid #ccc',
    borderRadius: '10px',
		boxSizing: 'border-box',
		outline: 'none'
}
const tabStyles = {
  headline: {
    fontSize: 24,
    paddingTop: 16,
    marginBottom: 12,
    fontWeight: 400,
  },
};

const buttonStyle = {
	background: 'transparent',
	borderWidth: 0,
	outline: 'none'
}

export default HomePage;