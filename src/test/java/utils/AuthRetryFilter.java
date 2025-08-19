package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class AuthRetryFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        // Attach/refresh the Authorization header
        req.removeHeader("Authorization");
        req.header("Authorization", "Bearer " + TokenManager.getToken());

        // First attempt
        Response response = ctx.next(req, res);

        // If unauthorized, refresh and retry once
        if (response.getStatusCode() == 401) {
            System.out.println("[Auth] 401 received, refreshing token and retrying once...");
            TokenManager.forceRefresh();

            req.removeHeader("Authorization");
            req.header("Authorization", "Bearer " + TokenManager.getToken());

            response = ctx.next(req, res);
        }

        return response;
    }
}
