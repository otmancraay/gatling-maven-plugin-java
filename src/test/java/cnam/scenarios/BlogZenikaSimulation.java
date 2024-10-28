package cnam.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class BlogZenikaSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://blog.zenika.com")
            .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .acceptLanguageHeader("en-US,en;q=0.5")
            .acceptEncodingHeader("gzip, deflate")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

    ChainBuilder getBlogPage = exec(
            http("Get Blog Homepage")
                    .get("/")
                    .check(
                            css("li.menu-item a[href*='tous-les-articles']", "href").saveAs("tousLesArticlesUrl")
                    )
    );

    ChainBuilder getFirstArticle = exec(
            http("Get First Article")
                    .get("#{tousLesArticlesUrl}")
                    .check(
                            css("div.cm-posts article div.cm-featured-image a", "href").saveAs("articleUrl")
                    )
    ).exec(session -> {
                String articleUrl = session.getString("articleUrl");
                if (articleUrl == null) {
                    System.out.println("L'URL de l'article n'a pas été trouvée.");
                }
                return session;
            }).pause(1)
            .exec(
                    http("View First Article")
                            .get("#{articleUrl}")
                            .check(status().is(200))
            );
    ScenarioBuilder basicScenario = scenario("Blog Zenika Scenario")
            .exec(getBlogPage)
            .exec(getFirstArticle);

    {
        setUp(
                basicScenario.injectOpen(
                        atOnceUsers(1),
                        nothingFor(5),
                        atOnceUsers(1)
                )
        ).protocols(httpProtocol);
    }

}
