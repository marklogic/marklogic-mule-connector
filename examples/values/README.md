Query to insert data for test demo:

declareUpdate();

var d = {
	"emp_no": "0001",
	"name": {
		"first": "Bob",
		"last": "Smith"
	},
	"favorite_colors": ["red",
	"white",
	"blue"],
	"hire_date": "01/01/2018",
	"birth_date": "01/01/1970",
	"gender": "male"
};

xdmp.documentInsert("/test/doc1.json", d);

d = {
	"emp_no": "0002",
	"hire_date": "01/03/2018",
	"name": {
		"first": "Mary",
		"last": "Merryweather"
	},
	"favorite_colors": ["green",
	"orange",
	"purple"],
	"birth_date": "01/01/1971",
	"gender": "female"
};

xdmp.documentInsert("/test/doc2.json", d);

Note : Had to change "Query Docs from MarkLogic" to use the output connector settings because input connector was not working.

Notes to test values:
Uses same data as marklogic-demo-1.1.0.xml .
Create string range index with default collation on emp_no and first.
Install these options with PUT /v1/config/query/employee :

<options xmlns="http://marklogic.com/appservices/search">
    <values name="EmployeeNumber">
        <range type="xs:string">
            <element ns="" name="emp_no"/>
        </range>
    </values>
    <values name="FirstName">
        <range type="xs:string">
            <element ns="" name="first"/>
        </range>
    </values>
</options>

cURL I used: 

curl --anyauth --user admin:admin -T './options.xml' -i -H "Content-type:application/xml" http://localhost:8010/v1/config/query/employee

To test : GET /v1/values/EmployeeNumber?options=employee&format=json

cURL I used: 
curl --anyauth --user admin:admin http://localhost:8010/v1/values/EmployeeNumber?options=employee&format=json