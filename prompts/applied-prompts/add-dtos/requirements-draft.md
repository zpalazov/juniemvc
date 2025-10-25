Update the BeerController to use DTOs. In the package `model`, create a new POJO called BeerDto with the same 
properties as the Beer entity. Update the BeerController to use BeerDto for request and response bodies instead of the 
Beer entity directly. Create a mapstruct mapper to convert between Beer and BeerDto. Mappers should be added to a 
package called `mapper`. When converting from a DTO to an entity, ignore the properties id, createDate, and updateDate.
Convert the service layer to use DTO objects and to use mappers to convert between entities and DTOs. Update the 
controllers to use DTOs.  
