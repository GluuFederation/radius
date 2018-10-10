package org.gluu.radius.util;

import java.util.Arrays;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.gluu.radius.GluuRadiusException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;


@RunWith(Parameterized.class)
public class CryptoUtilTest {

	private String plainpassword;

	@Parameters 
	public static Iterable<? extends Object> testPasswords() {

		return Arrays.asList("p@ssw@rd","gluu@p@ss","&qYT:86q]{CqbJg");
	}

	public CryptoUtilTest(String plainpassword) {
		this.plainpassword = plainpassword;
	}

	@Test
	public void passwordIsEncrypted() {

		String encryptedpassword = CryptoUtil.encryptPassword(plainpassword,"D5DEu3FF8LGYaxbNoXahfx7l");
		assertNotEquals(encryptedpassword,plainpassword);
		String decryptedpassword = CryptoUtil.decryptPassword(encryptedpassword,"D5DEu3FF8LGYaxbNoXahfx7l");
		assertEquals(plainpassword,decryptedpassword);
	}


	@Test
	public void plainPasswordIsNull() {

		try {
			String plainpassword = null;
			CryptoUtil.encryptPassword(plainpassword,"D5DEu3FF8LGYaxbNoXahfx7l");
		}catch(GluuRadiusException e) {
			assertThat(e.getMessage(),is("Invalid password specified"));
		}
	}


	@Test
	public void encryptedPasswordIsNull() {

		try {
			String encryptedpassword = null;
			CryptoUtil.decryptPassword(encryptedpassword,"D5DEu3FF8LGYaxbNoXahfx7l");
		}catch(GluuRadiusException e) {
			assertThat(e.getMessage(),is("Invalid encrypted password specified"));
		}
	}

}