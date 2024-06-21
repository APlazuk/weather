# Weather Forecast Application

This application is a tool for retrieving and managing weather-related information.
It was developed using Spring MVC, Lombok, and Java SDK version 17.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

You need to have Java 17 and Maven installed on your machine.

### Usage

1. GET /location/coordinates: This endpoint accepts a city name as a request parameter and returns the coordinates for that city.
   `curl -X GET 'http://localhost:8080/location/coordinates?city=London'`
   
2. POST /weather-nowcast: This returns the current weather information and radar map for a location. It takes a pair of coordinates as a request parameter and the desired zoom level for the radar map as a request body.
   `curl -X POST -H "Content-Type: application/json" -d '{"lat": 51.5074, "lon": 0.1278, "mapZoom": "5"}' 'http://localhost:8080/weather-nowcast'  `

3. GET /weather-info: This endpoint gets the weather information for the provided latitude and longitude.
   `curl -X POST -H "Content-Type: application/json" -d '{"lat": 51.5074, "lon": 0.1278, "mapZoom": "5"}' 'http://localhost:8080/weather-nowcast'
`
### Generating OpenAPI Documentation
This project supports the generation of OpenAPI documentation for the REST APIs.

### Steps to Generate and View OpenAPI Documentation
1. **Generating Documentation:** The OpenAPI documentation is automatically generated when the application starts up. You don't need to perform any actions to generate it.
2. **Viewing the Documentation:** To view the generated OpenAPI documentation, start the server and navigate to {server-url}/swagger-ui.html in your web browser, where {server-url} is the base URL of your server. For a local server running on the default port, this would be http://localhost:8080/swagger-ui.html.
3. **Using maven plugin:** To generate OpenAPI .yaml locally run maven command: `mvn clean install`. The file will be generated in the directory: openapi/weather-service.yaml
