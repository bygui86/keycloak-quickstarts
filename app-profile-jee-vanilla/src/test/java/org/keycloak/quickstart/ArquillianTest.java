package org.keycloak.quickstart;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.quickstart.page.IndexPage;
import org.keycloak.quickstart.page.LoginPage;
import org.keycloak.quickstart.page.ProfilePage;
import org.keycloak.quickstart.profilejee.Controller;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by abstractj on 3/13/17.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ArquillianTest {

    private static final String WEBAPP_SRC = "src/main/webapp";

    @Page
    private IndexPage indexPage;

    @Page
    private LoginPage loginPage;

    @Page
    private ProfilePage profilePage;

    @Deployment(name = "service-jee-jaxrs", order = 1, testable = false)
    public static Archive<?> createTestArchive1() throws IOException {
        return ShrinkWrap.createFromZipFile(WebArchive.class,
                new File("../service-jee-jaxrs/target/service.war"));
    }

    @Deployment(name = "app-profile-vanilla", order = 2, testable = false)
    public static Archive<?> createTestArchive2() throws IOException {
        return ShrinkWrap.create(WebArchive.class, "app-profile-vanilla.war")
                .addPackages(true, Filters.exclude(".*Test.*"), Controller.class.getPackage())
                .addAsWebResource(new File(WEBAPP_SRC, "index.jsp"))
                .addAsWebResource(new File(WEBAPP_SRC, "profile.jsp"))
                .addAsWebResource(new File(WEBAPP_SRC, "styles.css"))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .setWebXML(new File("src/main/webapp", "WEB-INF/web.xml"));
    }

    @Drone
    private WebDriver webDriver;

    @ArquillianResource
    @OperateOnDeployment("app-profile-vanilla")
    private URL contextRoot;

    @Before
    public void setup() {
        webDriver.navigate().to(contextRoot);
    }

    @Test
    public void testLogin() throws InterruptedException {
        try {
            indexPage.clickLogin();
            loginPage.login("admin", "admin");
            assertNotNull(profilePage.getUsername());
            profilePage.clickLogout();
        } catch (Exception e) {
            fail("Should display logged in user");
        }
    }
}
