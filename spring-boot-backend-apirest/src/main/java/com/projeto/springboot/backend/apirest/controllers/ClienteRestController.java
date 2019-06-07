package com.projeto.springboot.backend.apirest.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.springboot.backend.apirest.models.entity.Cliente;
import com.projeto.springboot.backend.apirest.models.services.IClienteService;

@CrossOrigin(origins = { "http://localhost:4200" })
@RestController
@RequestMapping("/api")
public class ClienteRestController {

	@Autowired
	private IClienteService clienteService;
	
	private final Logger log = LoggerFactory.getLogger(ClienteRestController.class);

	@GetMapping("/clientes")
	public List<Cliente> index() {
		return clienteService.findAll();
	}
	
	@GetMapping("/clientes/page/{page}")
	public Page<Cliente> index(@PathVariable Integer page) {
		Pageable pageable = PageRequest.of(page, 4);
		return clienteService.findAll(pageable);
	}
	
	@GetMapping("/clientes/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		
		Cliente cliente = null;
		Map<String, Object> response = new HashMap<>();
		
		try {
			 cliente = clienteService.findById(id);
		}
		
		catch(DataAccessException e) {
			response.put("mensagem", "Error ao realizar consulta ao Banco de Dados");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
		    return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if(cliente ==null) {
			response.put("mensagem", "O cliente da ID: ".concat(id.toString().concat(" nao existe no Banco de Dados")));
		    return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	}

	@PostMapping("/clientes")
	public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result) {
		
		Cliente clienteNew = null;
		Map<String, Object> response = new HashMap<>();
		
		if(result.hasErrors()) {
			List<String> errors  = result.getFieldErrors()
					        .stream()
				        	.map(err ->"O campo '" + err.getField() +"' " + err.getDefaultMessage())
				            .collect(Collectors.toList());
			
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			clienteNew = clienteService.save(cliente);
		} catch(DataAccessException e) {
			response.put("mensagem", "Error ao realizar o insert ao Banco de Dados");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		response.put("mensagem", "O cliente foi criado com sucesso");
		response.put("cliente", clienteNew);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PutMapping("/clientes/{id}")
	public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id) {

		Cliente clienteAtual = clienteService.findById(id);
		
		Cliente clienteUpdated = null;

		Map<String, Object> response = new HashMap<>();
		
		if(result.hasErrors()) {
			List<String> errors  = result.getFieldErrors()
				        	.stream()
			        		.map(err ->"O campo '" + err.getField() +"' " + err.getDefaultMessage())
			            	.collect(Collectors.toList());
			
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(clienteAtual ==null) {
			response.put("mensagem", "Error ao realizar ao alterar, pois o cliente da ID: "
					          .concat(id.toString().concat(" nao existe no Banco de Dados")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		
		try {
			
    		clienteAtual.setSobrenome(cliente.getSobrenome());
    		clienteAtual.setNome(cliente.getNome());
    		clienteAtual.setEmail(cliente.getEmail());
	    	clienteAtual.setCreateAt(cliente.getCreateAt());

		clienteUpdated = clienteService.save(clienteAtual);
		
		} catch(DataAccessException e) {
			    response.put("mensagem", "Error ao alterar cliente no Banco de Dados");
			    response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			    return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		response.put("mensagem", "O cliente foi atualizado com sucesso");
		response.put("cliente", clienteUpdated);
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

	}

	@DeleteMapping("/clientes/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		
		Map<String, Object> response = new HashMap<>();
		
		try {
			Cliente cliente = clienteService.findById(id);
			String nomeFotoAnterior = cliente.getFoto();
			
			if(nomeFotoAnterior !=null && nomeFotoAnterior.length() >0) {
				Path rotaFotoAnterior = Paths.get("uploads").resolve(nomeFotoAnterior).toAbsolutePath();
				File arquivoFotoAnterior = rotaFotoAnterior.toFile();
				if(arquivoFotoAnterior.exists() && arquivoFotoAnterior.canRead()) {
					arquivoFotoAnterior.delete();
				}
			}
			
		    clienteService.delete(id);
		} catch (DataAccessException e) {
			response.put("mensagem", "Error ao eliminar o cliente do Banco de Datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		response.put("mensagem", "O cliente deletado com exito!");
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@PostMapping("/clientes/upload")
	public ResponseEntity<?> upload(@RequestParam("arquivo") MultipartFile arquivo, @RequestParam("id") Long id){
		Map<String, Object> response = new HashMap<>();
		
		Cliente cliente = clienteService.findById(id);
		
		if(!arquivo.isEmpty()) {
			String nomeArquivo = UUID.randomUUID().toString() + "_" +  arquivo.getOriginalFilename().replace(" ", "");
			
			Path rotaArquivo = Paths.get("uploads").resolve(nomeArquivo).toAbsolutePath();
			log.info(rotaArquivo.toString());
			
			try {
				Files.copy(arquivo.getInputStream(), rotaArquivo);
			} catch (IOException e) {
				response.put("mensagem", "Error ao subir a imagem do cliente " + nomeArquivo);
				response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			String nomeFotoAnterior = cliente.getFoto();
			
			if(nomeFotoAnterior !=null && nomeFotoAnterior.length() >0) {
				Path rotaFotoAnterior = Paths.get("uploads").resolve(nomeFotoAnterior).toAbsolutePath();
				File arquivoFotoAnterior = rotaFotoAnterior.toFile();
				if(arquivoFotoAnterior.exists() && arquivoFotoAnterior.canRead()) {
					arquivoFotoAnterior.delete();
				}
			}
			
			cliente.setFoto(nomeArquivo);
			
			clienteService.save(cliente);
			
			response.put("cliente", cliente);
			response.put("mensagem", "Voce subido corretamente a imagem: " + nomeArquivo);
			
		}
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@GetMapping("/uploads/img/{nomeFoto:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String nomeFoto){
		
		Path rotaArquivo = Paths.get("uploads").resolve(nomeFoto).toAbsolutePath();
		
		log.info(rotaArquivo.toString());
		
		Resource recurso = null;
		
		try {
			recurso = new UrlResource(rotaArquivo.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if(!recurso.exists() && !recurso.isReadable()) {
			throw new RuntimeException("Error: A imagem não se pode carregada: " + nomeFoto);
		}
		HttpHeaders cabecera = new HttpHeaders();
		cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");
		
		return new ResponseEntity<Resource>(recurso, cabecera, HttpStatus.OK);
	}
}