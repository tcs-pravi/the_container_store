package com.containerstore.prestonintegrations.proposal.shared.proposalconstants;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class ProposalConstantsTest {

    @Test
    public void testProposalConstants() {
        MatcherAssert.assertThat(ProposalConstants.class, hasValidBeanConstructor());
        MatcherAssert.assertThat(ProposalConstants.class, hasValidGettersAndSetters());
        MatcherAssert.assertThat(ProposalConstants.class, hasValidBeanToString());
    }
}
