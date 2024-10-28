package cnam.scenarios;


import java.util.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class RecordedCnamSimulation extends Simulation {
    private static final String PASSWORD = "";
    private static final String USERNAME = "";

    {
        HttpProtocolBuilder httpProtocol = http
                .baseUrl("https://cnam-poc.projects.legisway.com")
                .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .acceptEncodingHeader("gzip, deflate, br")
                .acceptLanguageHeader("en-US,en;q=0.9")
                .upgradeInsecureRequestsHeader("1")
                .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");

        Map<CharSequence, String> headers_0 = new HashMap<>();
        headers_0.put("Cache-Control", "max-age=0");
        headers_0.put("Origin", "https://cnam-poc.projects.legisway.com");
        headers_0.put("Sec-Fetch-Dest", "document");
        headers_0.put("Sec-Fetch-Mode", "navigate");
        headers_0.put("Sec-Fetch-Site", "same-origin");
        headers_0.put("Sec-Fetch-User", "?1");
        headers_0.put("sec-ch-ua", "Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99");
        headers_0.put("sec-ch-ua-mobile", "?0");
        headers_0.put("sec-ch-ua-platform", "macOS");

        Map<CharSequence, String> headers_1 = new HashMap<>();
        headers_1.put("Sec-Fetch-Dest", "document");
        headers_1.put("Sec-Fetch-Mode", "navigate");
        headers_1.put("Sec-Fetch-Site", "same-origin");
        headers_1.put("sec-ch-ua", "Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99");
        headers_1.put("sec-ch-ua-mobile", "?0");
        headers_1.put("sec-ch-ua-platform", "macOS");

        ChainBuilder authentification = exec(
                http("Authentification")
                        .post("/j_spring_security_check")
                        .headers(headers_0)
                        .formParam("j_username", USERNAME)
                        .formParam("j_password", PASSWORD)
                        .formParam("mfa.retain.info", "")
                        .resources(
                                http("Redirection to /ng")
                                        .get("/ng")
                                        .headers(headers_1)
                                        .check(
                                                status().is(200)
                                        )
                        )
        );

        ChainBuilder getHomepage = exec(
                http("Get Homepage (Dossiers)")
                        .get("/ng/#/dashboard/repository:cfg%23af9e0630-255a-467e-9aa0-30a4c6ccfd0f/repository:cfg%23b55fbcb2-cdfd-4423-8247-b965c11ac972")
                        .check(
                                status().is(304)
                        )
        );

        ChainBuilder getCalenderPage = exec(
                http("Get Calender Page")
                        .get("/resource/absence/canaccessabsence")
                        .headers(headers_1)
                        .resources(
                                http("calender_request_1")
                                        .get("/resource/applicationModules/repository%3Acfg%23af9e0630-255a-467e-9aa0-30a4c6ccfd0f/repository%3Acfg%230c953fdb-31ab-4315-9acd-c4e838a132eb?noCache=b80f4378-1ce3-4a8f-bf42-cbf78a15873e")
                                        .headers(headers_1),
                                http("calender_request_2")
                                        .post("/resource/grid?noCache=cdcb866f-4727-4759-a304-a6524fd3188e&take=1000000&skip=0&page=1&pageSize=1000000")
                                        .headers(headers_1)
                                        .body(RawFileBody("calendar_request.json"))
                        )
        )
                .pause(16)
                .exec(
                        http("calender_request_3")
                                .get("/resource/discussions/pollheader")
                                .headers(headers_1)
                );
        ScenarioBuilder basicScenario = scenario("Cnam test Scenario")
                .exec(authentification)
                .exec(getHomepage)
                .exec(getCalenderPage);


        setUp(
                basicScenario.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        atOnceUsers(1)
                )
        ).protocols(httpProtocol);
    }
}
