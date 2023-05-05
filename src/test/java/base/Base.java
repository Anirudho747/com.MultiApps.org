package base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;

public class Base {

    protected static ThreadLocal <AppiumDriver> driver = new ThreadLocal<AppiumDriver>();
    protected static  ThreadLocal<Properties> props = new ThreadLocal<Properties>();
    protected static AppiumDriverLocalService server;

    public Base()
    {
    }

    public void setDriver(AppiumDriver driver2)
    {
        driver.set(driver2);
    }

    public AppiumDriver getDriver()
    {
        return driver.get();
    }

    public void setProps(Properties props2)
    {
        props.set(props2);
    }

    public Properties getProps()
    {
        return props.get();
    }

    public static boolean checkIfServerIsRunnning(int port) throws Exception {
        boolean isAppiumServerRunning = false;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            socket.close();
        } catch (IOException e) {
            System.out.println("1");
            isAppiumServerRunning = true;
        } finally {
            socket = null;
        }
        return isAppiumServerRunning;
    }


    public void initializeDriver() {

        boolean flag= false;
        try {
            flag = checkIfServerIsRunnning(4727);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!flag)
        {
            server = AppiumDriverLocalService.buildDefaultService();
            server.start();
        }

        try {
            FileInputStream fis;
            Properties props = new Properties();
            AppiumDriver driver;
            URL url;
            String propFileName = System.getProperty("user.dir") +"/src/test/resources/Global.properties";

            fis = new FileInputStream(propFileName);
            props.load(fis);
            String platformName = (String) props.get("platformName");
            String androidEmulatorAlowed = (String) props.get("androidEmulator");
            String iOSEmulatorAlowed = (String) props.get("iOSEmulator");
            String androidAppLocation = System.getProperty("user.dir") + props.get("androidAppLocation");
            String iOSAppLocation=System.getProperty("user.dir") + props.get("iOSAppLocation");

            DesiredCapabilities dc = new DesiredCapabilities();

            switch (platformName) {
                case "Android":
                    dc.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true);
                    dc.setCapability(MobileCapabilityType.PLATFORM_NAME, props.get("platformName"));

                    if(androidEmulatorAlowed.equalsIgnoreCase("true"))
                    {
                        dc.setCapability("platformVersion",props.get("androidPlatformVersion"));
                        //           dc.setCapability("avd", props.get("androidDevice"));
                        dc.setCapability(MobileCapabilityType.DEVICE_NAME,props.get("androidDevice"));
                    }
                    else
                    {
                        dc.setCapability("udid", props.get("androidUDID"));
                    }
                    dc.setCapability(MobileCapabilityType.APP, androidAppLocation);
                    dc.setCapability(MobileCapabilityType.AUTOMATION_NAME, props.get("androidAutomationName"));

                    url = new URL(props.getProperty("appiumURL") + "4723/wd/hub");
                    driver= new AndroidDriver(url,dc);
                    break;


                case "iOS":
                    dc.setCapability(MobileCapabilityType.PLATFORM_NAME, props.get("platformName"));
                    if(iOSEmulatorAlowed.equalsIgnoreCase("true"))
                    {
                        dc.setCapability("platformVersion",props.get("iOSPlatformVersion"));
                        dc.setCapability("avd", props.get("iOSDevice"));
                    }
                    else
                    {
                        dc.setCapability("udid", props.get("iosUDID"));
                    }
                    dc.setCapability(MobileCapabilityType.APP, iOSAppLocation);
                    dc.setCapability(MobileCapabilityType.AUTOMATION_NAME, props.get("iOSAutomationName"));

                    url = new URL(props.getProperty("appiumURL") + "4724/wd/hub");
                    driver = new IOSDriver(url, dc);
                    break;

                default :
                    throw new Exception("Invalid platform! =");
            }
            setDriver(driver);
            String sessionId = driver.getSessionId().toString();
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
    }
}
