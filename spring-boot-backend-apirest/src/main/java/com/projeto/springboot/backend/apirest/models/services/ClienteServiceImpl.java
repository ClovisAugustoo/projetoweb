package com.projeto.springboot.backend.apirest.models.services;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.springboot.backend.apirest.models.dao.IClienteDao;
import com.projeto.springboot.backend.apirest.models.entity.Cliente;

@Service
public class ClienteServiceImpl implements IClienteService {

	@Autowired
	private IClienteDao clienteDao;
	
    @Override
	@Transactional(readOnly = true)
    public List<Cliente> findAll() {
    return (List<Cliente>) clienteDao.findAll();
    }
	/*
	@Override
	@Transactional(readOnly = true)
	public Page<Cliente> findAll(Pageable pageable) {
		return clienteDao.findAll(pageable);
	}
    */
	@Override
	public Page<Cliente> findAll(org.springframework.data.domain.Pageable pageable) {
		return clienteDao.findAll(pageable);
	}
    
	@Override
	@Transactional(readOnly = true)
	public Cliente findById(Long id) {
		return clienteDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public Cliente save(Cliente cliente) {
		return clienteDao.save(cliente);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		clienteDao.deleteById(id);
	}
}