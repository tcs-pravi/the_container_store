package com.containerstore.prestonintegrations.proposal;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("local-test")
@RunWith(SpringRunner.class)
public class ApplicationTest {

	@Test
	public void contextLoads() {
		assertThrows(IllegalArgumentException.class, () -> Application.main(null));
	}
}
