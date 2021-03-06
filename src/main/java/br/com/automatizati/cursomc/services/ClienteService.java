package br.com.automatizati.cursomc.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.automatizati.cursomc.domain.Cidade;
import br.com.automatizati.cursomc.domain.Cliente;
import br.com.automatizati.cursomc.domain.Endereco;
import br.com.automatizati.cursomc.domain.enums.Perfil;
import br.com.automatizati.cursomc.domain.enums.TipoCliente;
import br.com.automatizati.cursomc.dto.ClienteDTO;
import br.com.automatizati.cursomc.dto.ClienteNewDTO;
import br.com.automatizati.cursomc.repositories.CidadeRepository;
import br.com.automatizati.cursomc.repositories.ClienteRepository;
import br.com.automatizati.cursomc.repositories.EnderecoRepository;
import br.com.automatizati.cursomc.security.UserSS;
import br.com.automatizati.cursomc.services.exceptions.AuthorizationException;
import br.com.automatizati.cursomc.services.exceptions.DataIntegrityException;
import br.com.automatizati.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private BCryptPasswordEncoder pe;

	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private CidadeRepository cidadeRepository;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	
	public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if(user==null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Cliente obj = repo.findOne(id);
		if(obj == null) {
			throw new ObjectNotFoundException("Objeto não encontrado! Id:" + id + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}
	
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.save(obj.getEnderecos());
		return obj;
		
	}
	
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);
	}
	
	public void delete(Integer id) {
		find(id);
		try {
			repo.delete(id);
		}
		catch (DataIntegrityViolationException e){
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionadas");
		}
	}
	
	public List<Cliente> findAll(){
		return repo.findAll();
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = new PageRequest(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.getId(), objDto.getNome(), objDto.getEmail(), null, null, null);
	}
	
	public Cliente fromDTO(ClienteNewDTO objDto) {
		Cliente cli = new Cliente(null, objDto.getNome(), objDto.getEmail(), objDto.getCpfOuCnpj(), TipoCliente.toEnum(objDto.getTipo()), pe.encode(objDto.getSenha()));
		Cidade cid = cidadeRepository.findOne(objDto.getCidadeId());
		Endereco end = new Endereco(null, objDto.getLogradouro(), objDto.getNumero(), objDto.getComplemento(), objDto.getBairro(), objDto.getCep(), cli, cid );
		cli.getEnderecos().add(end);
		cli.getTelefones().add(objDto.getTelefone1());
		if(objDto.getTelefone2() !=null) {
			cli.getTelefones().add(objDto.getTelefone2());			
		}
		if(objDto.getTelefone3() !=null) {
			cli.getTelefones().add(objDto.getTelefone3());			
		}
		return cli;
	}
	
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
}
