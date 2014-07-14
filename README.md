Recipes API
===========

An API for Guardian recipes.

## Endpoints

    /
    /recipes
    /recipes/{id}

## Search

A search endpoint is also provided

    /search/recipes?q=???

where the 'q' parameter is a '+' separated list of search qualifiers
of the form key:value. For example:

    /search/recipes?q=cake+ingredient:chocolate+maxCookTime:60

Supported search qualifiers are (initially):

    ingredient
    maxCookTime
    maxPrepTime
    recipeCuisine

Search requests also support sort/order parameters:

   sort=[score|indexed, default=score]
   order=[asc|desc, default=desc]
