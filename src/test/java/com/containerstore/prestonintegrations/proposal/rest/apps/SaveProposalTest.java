package com.containerstore.prestonintegrations.proposal.rest.apps;

import com.containerstore.spring.boot.starters.rest.DefaultExceptionHandlers;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@SpringBootTest
public class SaveProposalTest {

    private MockMvc mockMvc;

    @Autowired
    private ClosetProProposalResource closetProProposalResource;

    public static WireMockServer wiremock = new WireMockServer(WireMockSpring
            .options()
            .usingFilesUnderClasspath("wiremock/SaveProposal")
            .port(8080)
            .bindAddress("127.0.0.1"));

    @BeforeAll
    static void setupClass() {
        wiremock.start();
    }

    @AfterEach
    void after() {
        wiremock.resetAll();
    }

    @AfterAll
    static void clean() {
        wiremock.shutdown();
    }

    @BeforeEach
    public void setMockMvc(){
        mockMvc = MockMvcBuilders.standaloneSetup(closetProProposalResource).setControllerAdvice(new DefaultExceptionHandlers()).build();
    }

    @Test
    public void test_sendProposalToSalesforce() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXjKAAX",
                                    "proposalId": "1331",
                                    "spaces": [
                                        {
                                            "spaceId": "569167",
                                            "retailPrice": 4169.46,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1557.29
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.34
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 1184.41
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "569168",
                                            "retailPrice": 8742.74,
                                            "sellingPrice": null,
                                            "color": "White Velvet",
                                            "finish": "Slate",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 3265.42
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.33
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 2483.53
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "569169",
                                            "retailPrice": 3921.71,
                                            "sellingPrice": null,
                                            "color": "Granite",
                                            "finish": "Slate",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1464.76
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.33
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 1114.03
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerFees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 1000
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 440
                                            }
                                        ]
                                    },
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 999.94,
                                            "hasError": null,
                                            "errorMessage": null
                                        }
                                    ],
                                    "offers": [
                                        {
                                            "offerCode": "PRAMT50",
                                            "offerName": null,
                                            "adjustmentValue": null,
                                            "adjustmentType": null,
                                            "amountOff": 5244.0,
                                            "hasApplied": null,
                                            "hasError": null,
                                            "errorMessage": null
                                        }
                                    ],
                                    "customFees": null,
                                    "pdfUrl": null
                                }
                """))
                .andExpect(status().is(200)).andReturn();
    }

    @Test
    public void test_sendProposal_for_empty_cases_response() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXD2AA",
                                    "proposalId": 1008,
                                    "spaces": [
                                        {
                                            "spaceId": 569160,
                                            "retailPrice": 3922.99,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerfees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 240
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 660
                                            }
                                        ]
                                    },
                                    "offers": [
                                        {
                                            "offerCode": "PRAMT50",
                                            "amountOff": 50.0
                                        }
                                    ],
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 843.88
                                        }
                                    ]
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validations failed, Please check the CSD status in Salesforce for opportunity id: 0067e00000QNXD2AA"))
                .andDo(print());
    }

    @Test
    public void test_sendProposal_with_two_stackable_offers() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXD2AAP",
                                    "proposalId": 1008,
                                    "spaces": [
                                        {
                                            "spaceId": 569160,
                                            "retailPrice": 3922.99,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": 569161,
                                            "retailPrice": 3922.99,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerfees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 240
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 660
                                            }
                                        ]
                                    },
                                    "offers": [
                                        {
                                            "offerCode": "PRAMT25",
                                            "amountOff": 25.0
                                        },
                                        {
                                            "offerCode": "PRAMT50",
                                            "amountOff": 50.0
                                        }
                                    ],
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 843.88
                                        }
                                    ]
                                }
                """))
                .andExpect(status().is(200)).andReturn();
    }

    @Test
    public void test_sendProposal_with_stackable_and_nonstackable_offers() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXD2AAP",
                                    "proposalId": 1008,
                                    "spaces": [
                                        {
                                            "spaceId": 569160,
                                            "retailPrice": 3922.99,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": 569161,
                                            "retailPrice": 3922.99,
                                            "sellingPrice": null,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerfees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 240
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 660
                                            }
                                        ]
                                    },
                                    "offers": [
                                        {
                                            "offerCode": "PRAMT25",
                                            "amountOff": 25.0
                                        },
                                        {
                                            "offerCode": "PRSTNEXP",
                                            "amountOff": 500.0
                                        }
                                    ],
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 843.88
                                        }
                                    ]
                                }
                """))
                .andExpect(status().is(200)).andReturn();
    }

    @Test
    public void test_sendProposal_with_expired_offer() throws Exception {
       mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXD2AAP",
                                    "proposalId": "1008",
                                    "spaces": [
                                        {
                                            "spaceId": "1",
                                            "retailPrice": 3922.99,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "typeOfCloset": "Reach-in",
                                            "useOfSpace": "Primary",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "2",
                                            "retailPrice": 3922.99,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "typeOfCloset": "Reach-in",
                                            "useOfSpace": "Primary",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerFees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 240
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 660
                                            }
                                        ]
                                    },
                                    "offers": [
                                        {
                                            "offerCode": "PRESTOC",
                                            "amountOff": 0
                                        }
                                    ],
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 843.88
                                        }
                                    ]
                                }
                """))
                .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
               .andExpect(jsonPath("$.message").value("Invalid offer codes: [PRESTOC]"))
               .andDo(print());
    }

    @Test
    public void test_sendProposal_with_duplicate_spaces() throws Exception {
        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXD2AAP",
                                    "proposalId": "1008",
                                    "spaces": [
                                        {
                                            "spaceId": "1",
                                            "retailPrice": 3922,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "typeOfCloset": "Reach-in",
                                            "useOfSpace": "Primary",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "1",
                                            "retailPrice": 3922.99,
                                            "color": "Chalk",
                                            "finish": "Nickel",
                                            "typeOfCloset": "Reach-in",
                                            "useOfSpace": "Primary",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1628.04
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 3200
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerFees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 240
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 660
                                            }
                                        ]
                                    },
                                    "offers": [
                                        {
                                            "offerCode": "PRESTOC",
                                            "amountOff": 0
                                        }
                                    ],
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 843.88
                                        }
                                    ]
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Validations failed, SaveProposalRequest contains duplicate spaces"))
                .andDo(print());
    }

    @Test
    public void test_invalid_sendProposal() throws Exception {

        mockMvc.perform(post("/apps/closetpro/api/v1/proposals/salesforce/send-proposal").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content("""
                        {
                                    "opportunityId": "0067e00000QNXjKAAX",
                                    "proposalId": "1331",
                                    "spaces": [
                                        {
                                            "spaceId": "569167",
                                            "retailPrice": 4169.46,
                                            "sellingPrice": null,
                                            "finish": "Nickel",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1557.29
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.34
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 1184.41
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "569167",
                                            "retailPrice": 8742.74,
                                            "sellingPrice": null,
                                            "color": "White Velvet",
                                            "finish": "Slate",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 3265.42
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.33
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 2483.53
                                                }
                                            ]
                                        },
                                        {
                                            "spaceId": "569169",
                                            "retailPrice": 3921.71,
                                            "sellingPrice": null,
                                            "color": "Granite",
                                            "finish": "Slate",
                                            "spaceFees": [
                                                {
                                                    "name": "INSTALLATION_FEE",
                                                    "fee": 1464.76
                                                },
                                                {
                                                    "name": "ADDITIONAL_SERVICES_DEMOLITION",
                                                    "fee": 301.33
                                                },
                                                {
                                                    "name": "FREIGHT_FEE",
                                                    "fee": 1114.03
                                                }
                                            ]
                                        }
                                    ],
                                    "fees": {
                                        "headerFees": [
                                            {
                                                "name": "ADDITIONAL_SERVICES_FEE",
                                                "fee": 1000
                                            },
                                            {
                                                "name": "ADDITIONAL_SERVICES_TRANSPORTATION",
                                                "fee": 440
                                            }
                                        ]
                                    },
                                    "tax": [
                                        {
                                            "taxName": "SALES_TAX",
                                            "amount": 999.94,
                                            "hasError": null,
                                            "errorMessage": null
                                        }
                                    ],
                                    "offers": [
                                        {
                                            "offerCode": "PRAMT50",
                                            "amountOff": 5244.0
                                        }
                                    ],
                                    "customFees": null,
                                    "pdfUrl": null
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andDo(print());
    }
}
