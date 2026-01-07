# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, right now, it’s a bit of a mix: Product and Store use the Active Record pattern, while Warehouse is using the Repository pattern. 
If I were maintaining this long-term, I’d move everything over to the Repository pattern. It feels a lot cleaner to keep the persistence logic separate from the domain models.
Active Record is quick to set up, but as the project grows, those entity classes can become overburdened with persistence details. The Repository approach makes the code easier to test and keeps our domain models focused on business logic.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches have their place, but they definitely feel different. Coding directly, as in Product and Store, is great for moving fast and keeping everything in Java. But the details in the code and documentation can easily drift apart. 
Generating code from an OpenAPI spec, as in Warehouse, is much better for a professional setup. It forces a Design-First approach where you agree on the API contract before you start building. It makes it easier for external parties consuming the API because they have a clear spec to work with.
Personally, I'd go with the Design-First approach for the whole project. It's more robust, keeps the docs in sync, and provides a better developer/consumer experience overall.
```