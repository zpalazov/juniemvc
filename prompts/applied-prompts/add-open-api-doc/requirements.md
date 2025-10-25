Title: Update OpenAPI Documentation for Beer and Beer Order APIs

Objective
- Build clear, comprehensive, and accurate OpenAPI documentation that fully covers the Beer API and Beer Order API implemented in:
  - com.hophman.learning.juniemvc.rest.BeerController
  - com.hophman.learning.juniemvc.rest.BeerOrderController
- Follow the project’s OpenAPI structure and conventions under openapi-starter/openapi, and the Spring Boot and OpenAPI guidelines provided in the repository.

Scope
- Define all paths, operations, request parameters, request bodies, response codes, response bodies, and relevant headers for the following endpoints:
  - Beer API
    - POST /api/beers
    - GET /api/beers
    - GET /api/beers/{id}
    - PUT /api/beers/{id}
    - DELETE /api/beers/{id}
  - Beer Order API
    - POST /api/v1/beer-orders
    - GET /api/v1/beer-orders/{id}
- Provide and/or reuse component schemas for all request/response DTOs used by the above endpoints.
- Ensure error responses reference the existing Problem Details component if available.

Authoring locations and file naming
- Root spec file: openapi-starter/openapi/openapi.yaml
- Add path files under: openapi-starter/openapi/paths/
  - Use readable names that reflect the path by replacing slashes with underscores and keeping {param} braces, per project conventions:
    - /api/beers → paths/api_beers.yaml
    - /api/beers/{id} → paths/api_beers_{id}.yaml
    - /api/v1/beer-orders → paths/api_v1_beer-orders.yaml
    - /api/v1/beer-orders/{id} → paths/api_v1_beer-orders_{id}.yaml
- Add schema component files under: openapi-starter/openapi/components/schemas/
  - Beer.yaml
  - PlaceBeerOrderRequest.yaml
  - OrderItemRequest.yaml
  - BeerOrderResponse.yaml
  - BeerOrderLineResponse.yaml
  - Reuse existing components (e.g., Problem.yaml) if present; do not duplicate.
- Update openapi-starter/openapi/openapi.yaml:
  - Under paths:, add $ref entries for each new path file, for example:
    - /api/beers: $ref: 'paths/api_beers.yaml'
    - /api/beers/{id}: $ref: 'paths/api_beers_{id}.yaml'
    - /api/v1/beer-orders: $ref: 'paths/api_v1_beer-orders.yaml'
    - /api/v1/beer-orders/{id}: $ref: 'paths/api_v1_beer-orders_{id}.yaml'

Common conventions and types
- JSON property naming: camelCase.
- date-time fields use format: date-time (RFC 3339). For LocalDateTime map to type: string, format: date-time.
- Monetary/decimal values use type: number, format: double (or string if you prefer exactness; if using number, document typical precision). For this repo, use number to be consistent with other examples unless specified otherwise.
- IDs are integers (type: integer, format: int32).
- Apply validation constraints reflected in DTO annotations via schema metadata (minLength, pattern, minimum, etc.).
- Error responses: reference components/responses/Problem.yaml (application/problem+json) if present; otherwise, reference components/schemas/Problem.yaml inside a response definition. Prefer the response wrapper component if available in this repo.

Schemas to define (components/schemas)
- Beer (maps com.hophman.learning.juniemvc.model.BeerDto)
  - type: object
  - properties:
    - id: integer (int32), nullable: true
    - name: string, minLength: 1 (from @NotBlank)
    - style: string, minLength: 1 (from @NotBlank)
    - upc: string, minLength: 1 (from @NotBlank)
    - quantityOnHand: integer (int32), minimum: 0, nullable: true (from @PositiveOrZero)
    - price: number, format: double, minimum: 0 (strictly > 0 in code via @Positive; express as exclusiveMinimum: 0)
    - version: integer (int32), nullable: true
    - createdDate: string, format: date-time, nullable: true
    - updateDate: string, format: date-time, nullable: true
  - required: [name, style, upc, price]

- PlaceBeerOrderRequest (maps com.hophman.learning.juniemvc.rest.PlaceBeerOrderRequest)
  - type: object
  - properties:
    - customerRef: string, minLength: 1 (from @NotBlank)
    - items: array of OrderItemRequest, minItems: 1 (from @NotEmpty)
  - required: [customerRef, items]

- OrderItemRequest (maps com.hophman.learning.juniemvc.rest.OrderItemRequest)
  - type: object
  - properties:
    - beerId: integer (int32), minimum: 1, nullable: false (from @NotNull @Positive)
    - quantity: integer (int32), minimum: 1, nullable: false (from @NotNull @Positive)
  - required: [beerId, quantity]

- BeerOrderResponse (maps com.hophman.learning.juniemvc.rest.BeerOrderResponse)
  - type: object
  - properties:
    - id: integer (int32)
    - customerRef: string
    - status: string
    - paymentAmount: number, format: double, nullable: true
    - createdDate: string, format: date-time, nullable: true
    - updatedDate: string, format: date-time, nullable: true
    - lines: array of BeerOrderLineResponse
  - required: [id, customerRef, status, lines]

- BeerOrderLineResponse (maps com.hophman.learning.juniemvc.rest.BeerOrderLineResponse)
  - type: object
  - properties:
    - beerId: integer (int32)
    - beerName: string, nullable: true
    - orderQuantity: integer (int32)
    - status: string
  - required: [beerId, orderQuantity, status]

Paths and operations
- /api/beers (paths/api_beers.yaml)
  - post
    - summary: Create a beer
    - requestBody: application/json → schema: $ref ../components/schemas/Beer.yaml
    - responses:
      - 201 Created: body application/json → Beer; headers:
        - Location: string (URI of the created resource)
      - 400 Bad Request: $ref ../components/responses/Problem.yaml
    - description: Creates a new Beer; returns the created entity and sets Location header to /api/beers/{id}.
  - get
    - summary: List beers
    - responses:
      - 200 OK: application/json → schema: type: array, items: Beer

- /api/beers/{id} (paths/api_beers_{id}.yaml)
  - parameters:
    - path id: integer (int32), required: true
  - get
    - summary: Get beer by id
    - responses:
      - 200 OK: application/json → Beer
      - 404 Not Found: $ref ../components/responses/Problem.yaml
  - put
    - summary: Update beer by id
    - requestBody: application/json → Beer
    - responses:
      - 200 OK: application/json → Beer
      - 404 Not Found: $ref ../components/responses/Problem.yaml
      - 400 Bad Request: $ref ../components/responses/Problem.yaml
  - delete
    - summary: Delete beer by id
    - responses:
      - 204 No Content
      - 404 Not Found: $ref ../components/responses/Problem.yaml

- /api/v1/beer-orders (paths/api_v1_beer-orders.yaml)
  - post
    - summary: Place a beer order
    - requestBody: application/json → PlaceBeerOrderRequest
    - responses:
      - 201 Created: application/json → BeerOrderResponse
      - 400 Bad Request: $ref ../components/responses/Problem.yaml

- /api/v1/beer-orders/{id} (paths/api_v1_beer-orders_{id}.yaml)
  - parameters:
    - path id: integer (int32), required: true
  - get
    - summary: Get a beer order by id
    - responses:
      - 200 OK: application/json → BeerOrderResponse
      - 404 Not Found: $ref ../components/responses/Problem.yaml

Additional notes
- Security: not specified by controllers; omit unless a global scheme is already defined and required by the project.
- Headers: include Location for POST /api/beers (BeerController sets the Location header).
- Use tags to group operations logically:
  - tag: Beers → operations under /api/beers
  - tag: Beer Orders → operations under /api/v1/beer-orders
- Provide concise operation descriptions and examples where helpful (optional):
  - You may add example objects under components/examples or inline examples in request/response content.

Acceptance criteria
- All new path files exist and are referenced from openapi-starter/openapi/openapi.yaml.
- All schemas are added under components/schemas and referenced by the path files.
- The OpenAPI linter passes:
  - cd openapi-starter
  - npm install (first run or when dependencies change)
  - npm test (runs redocly lint) → exits with success and no blocking errors
- Optional: Preview docs locally:
  - cd openapi-starter && npm start (redocly preview-docs)
- The documentation clearly reflects the current behavior of the controllers and DTOs, including status codes and headers.

Testing and validation
- Use an OpenAPI linter (e.g., Redocly CLI) to validate the
