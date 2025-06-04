package com.pbl.mapmo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


class MapmoApplicationTests {

	@Test
	public void updatePasswordHash() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String knownPassword = "test12345"; // 또는 사용자가 원하는 비밀번호
		String newHash = encoder.encode(knownPassword);

		System.out.println("이 해시로 데이터베이스를 업데이트하세요: " + newHash);
		// UPDATE user SET password = '새해시값' WHERE email = 'test@test.com';
	}

}
