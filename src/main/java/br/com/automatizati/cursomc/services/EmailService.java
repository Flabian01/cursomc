package br.com.automatizati.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import br.com.automatizati.cursomc.domain.Cliente;
import br.com.automatizati.cursomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);
	
	void sendEmail(SimpleMailMessage msg);
	
	void sendNewPasswordEmail(Cliente cliente, String newPass);
}
