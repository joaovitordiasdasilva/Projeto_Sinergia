package br.com.sinergia.Sinergia.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.sinergia.Sinergia.model.Usuario;
import br.com.sinergia.Sinergia.model.dtos.CredenciaisDTO;
import br.com.sinergia.Sinergia.model.dtos.UsuarioLoginDTO;
import br.com.sinergia.Sinergia.repository.UsuarioRepository;

/**
 * 
 * @author Victor
 * @version 1.0
 */
@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository repository;

	private static String encriptadorDeSenha(String senha) {
		BCryptPasswordEncoder enconder = new BCryptPasswordEncoder();
		return enconder.encode(senha);
	}

	public Optional<Object> cadastrarUsuario(Usuario usuarioParaCadastrar) {
		return repository.findByEmail(usuarioParaCadastrar.getEmail()).map(usuarioExistente -> {
			return Optional.empty();
		}).orElseGet(() -> {
			usuarioParaCadastrar.setSenha(encriptadorDeSenha(usuarioParaCadastrar.getSenha()));
			return Optional.ofNullable(repository.save(usuarioParaCadastrar));
		});

	}
	public Optional<Usuario> atualizarUsuario(Usuario usuarioParaAtualizar) {
		return repository.findById(usuarioParaAtualizar.getIdUsuario()).map(resp -> {
			resp.setNomeCompleto(usuarioParaAtualizar.getNomeCompleto());
			resp.setEmail(usuarioParaAtualizar.getEmail());
			resp.setTipo(usuarioParaAtualizar.getTipo());
			resp.setFoto(usuarioParaAtualizar.getFoto());
			resp.setSenha(encriptadorDeSenha(usuarioParaAtualizar.getSenha()));
			return Optional.ofNullable(repository.save(resp));
		}).orElseGet(() -> {
			return Optional.empty();
		});

	}

	private static String gerarToken(String email, String senha) {
		String estrutura = email + ":" + senha;
		byte[] estruturaBase64 = Base64.encodeBase64(estrutura.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(estruturaBase64);

	}

	public ResponseEntity<CredenciaisDTO> pegarCredenciais(UsuarioLoginDTO usuarioParaAutenticar) {
		return repository.findByEmail(usuarioParaAutenticar.getEmail()).map(resp -> {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (encoder.matches(usuarioParaAutenticar.getSenha(), resp.getSenha())) {

				CredenciaisDTO objetoCredenciaisDTO = new CredenciaisDTO();

				objetoCredenciaisDTO.setToken(gerarToken(usuarioParaAutenticar.getEmail(), usuarioParaAutenticar.getSenha()));
				objetoCredenciaisDTO.setIdUsuario(resp.getIdUsuario());
				objetoCredenciaisDTO.setNome(resp.getNomeCompleto());
				objetoCredenciaisDTO.setEmail(resp.getEmail());
				objetoCredenciaisDTO.setSenha(resp.getSenha());
				objetoCredenciaisDTO.setTipo(resp.getTipo());
				objetoCredenciaisDTO.setFoto(resp.getFoto());

				return ResponseEntity.status(201).body(objetoCredenciaisDTO);
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha Incorreta!");
			}
		}).orElseGet(() -> {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não existe!");
		});

	}

}
