package com.containerstore.prestonintegrations.proposal.rest.internal;

import com.containerstore.prestonintegrations.proposal.store.dto.PaginatedAPIResponse;
import com.containerstore.prestonintegrations.proposal.store.dto.StoreDTO;
import com.containerstore.prestonintegrations.proposal.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/internal/api/v1/stores")
@RequiredArgsConstructor
public class StoreResource {

    private final StoreService storeService;

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaginatedAPIResponse<StoreDTO>> getStoreDetails(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(value = "state", required = false)
            @Length(max = 2, message = "Param state should be an abbreviation and of length 2.") String stateAbbreviation) {

        return ResponseEntity.ok().body(storeService.getPaginatedStoresList(PageRequest.of(page, size), stateAbbreviation));
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateStore(@RequestBody StoreDTO request) {
        return new ResponseEntity<>(storeService.updateStore(request), HttpStatus.OK);
    }

}
