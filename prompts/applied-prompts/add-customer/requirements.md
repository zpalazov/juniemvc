## Change Requirements
Add a new entity to the project called "Customer".

The JPA entity should have the following fields:
- name: String (Required) 
- email: String
- phone: number
- address line 1: String (Required)
- address line 2: String
- city: String (Required)
- state: String (Required)
- postal code: String (Required)

The Customer should have a one to many relationship with BeerOrder.
Add a flyway migration to create the customers table with the above fields and the relationship to BeerOrder.

Add a CustomerRepository interface that extends JpaRepository.
Add a CustomerService class with methods to create, read, update, and delete customers.
Add a CustomerController class with REST endpoints to handle CRUD operations for customers.
Use dto objects for transferring customer data in the service and controller layers.
Use mapstruct for mapping between entity and dto objects.
Update Open API documentation to include the new Customer's endpoints.

Testing
- rest layer should have unit tests using mockmvc
- service layer should have unit tests using mockito
- repository layer should have integration tests using @DataJpaTest
