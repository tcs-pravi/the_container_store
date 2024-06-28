package com.containerstore.prestonintegrations.proposal.salesforceintegration.mapper;


import org.mapstruct.Mapper;
import com.containerstore.prestonintegrations.proposal.models.SalesforceSaveProposalRequest;
import com.containerstore.prestonintegrations.proposal.models.SaveProposalRequest;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SaveProposalRequestMapper {

    SaveProposalRequestMapper SAVE_PROPOSAL_REQUEST_MAPPER = Mappers.getMapper(SaveProposalRequestMapper.class);
    SalesforceSaveProposalRequest saveProposalRequestToSalesforceSaveProposalRequest(SaveProposalRequest saveProposalRequest);
}
