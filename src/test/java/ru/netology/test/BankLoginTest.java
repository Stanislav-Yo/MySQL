package ru.netology.test;

import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.SQLHelper;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static ru.netology.data.SQLHelper.cleanAuthCodes;
import static ru.netology.data.SQLHelper.cleanDatabase;

public class BankLoginTest {
    LoginPage loginPage;
    DataHelper.AuthInfo authInfo = DataHelper.getAuthInfoWithTestData();

    @AfterAll
    static void tearDownAll(){
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanAuthCodes();
    }

    @BeforeEach
    void setUp() {
        loginPage = open("http://localhost:9999", LoginPage.class);
    }

    @Test
    @DisplayName("Should successfully login to dashboard with exist login and password from sut test data")
    void shouldSuccessfullyLogin() {
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = SQLHelper.getVerificationCode();
        verificationPage.validVerify(verificationCode.getCode());
    }

    @Test
    @DisplayName("Should get error notification if user is not exist in base")
    void randomUserWithoutAddingToBase() {
        var authInfo = DataHelper.generateRandomUser();
        loginPage.login(authInfo);
        loginPage.verifyErrorNotification("Ошибка! \nНеверно указан логин или пароль");
    }

    @Test
    @DisplayName("Should get error notification if login with exist in base and active user and random notification code")
    void shouldGetErrorNotificationIfLoginWithExistInBaseAndRandomNotificationCode() {
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.generateRandomVerificationCode();
        verificationPage.verify(verificationCode.getCode());
        verificationPage.verifyErrorNotification("Ошибка! \nНеверно указан код! Попробуйте ещё раз.");
    }

    @Test
    @DisplayName("Should block system after 3 failed login attempts")
    void shouldBlockSystemAfterThreeFailedAttempts() {
        var validLogin = authInfo.getLogin();
        var invalidPassword1 = DataHelper.generateRandomPassword();
        var invalidPassword2 = DataHelper.generateRandomPassword();
        var invalidPassword3 = DataHelper.generateRandomPassword();
        loginPage.login(new DataHelper.AuthInfo(validLogin, invalidPassword1));
        loginPage.verifyErrorNotification("Ошибка! \nНеверно указан логин или пароль");
        loginPage = open("http://localhost:9999", LoginPage.class);
        loginPage.login(new DataHelper.AuthInfo(validLogin, invalidPassword2));
        loginPage.verifyErrorNotification("Ошибка! \nНеверно указан логин или пароль");
        loginPage = open("http://localhost:9999", LoginPage.class);
        loginPage.login(new DataHelper.AuthInfo(validLogin, invalidPassword3));
        loginPage.verifyErrorNotification("Ошибка! \nНеверно указан логин или пароль");
        loginPage = open("http://localhost:9999", LoginPage.class);
        loginPage.validLogin(authInfo);
        loginPage.verifyErrorNotification("Ошибка! \nСистема заблокирована. Попробуйте позже");
    }

}
