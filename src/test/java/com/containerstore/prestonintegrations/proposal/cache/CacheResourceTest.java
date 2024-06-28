package com.containerstore.prestonintegrations.proposal.cache;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("local-test")
@RunWith(SpringRunner.class)
@WebMvcTest(CacheResource.class)
class CacheResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CacheManager cacheManager;

    @Test
    void testStateEvict() throws Exception {;
        MvcResult result = mockMvc
                .perform(delete("/api/v1/admin/cache/evict")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("cache-alias", "STATE")).andExpect(status().is(202)).andReturn();
    }

    @Test
    void testProposalConstantEvict() throws Exception {
        MvcResult result = mockMvc
                .perform(delete("/api/v1/admin/cache/evict")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("cache-alias", "PROPOSALCONSTANTS")).andExpect(status().is(202)).andReturn();
    }

    @Test
    void testInvalidCacheRegionEvict() throws Exception {
        MvcResult result = mockMvc
                .perform(delete("/api/v1/admin/cache/evict")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("cache-alias", "UNKNOWN")).andExpect(status().is(400)).andReturn();
    }
}
