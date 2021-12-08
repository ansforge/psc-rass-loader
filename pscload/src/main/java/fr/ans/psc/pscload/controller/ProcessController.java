package fr.ans.psc.pscload.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class ProcessController {
	
	@PostMapping(value = "/continue")
    public DeferredResult<ResponseEntity<Void>> continueProcess() {
        return null;
    }

	
	@PostMapping(value = "/abort")
    public ResponseEntity<Void> abortProcess() {
        return null;
    }

}
