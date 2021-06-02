package encriptacionSimetrica;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Cliente extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataInputStream input;
	DataOutputStream output;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton boton = new JButton("Enviar");
	JButton salir = new JButton("Salir");
	boolean repetir = true;
	static boolean repetir2 = true;
	
	public Cliente(Socket socket, String nombre)
	
	{
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" Conexi�n del cliente: " + nombre);
		setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		textarea = new JTextArea();
		scrollpane = new JScrollPane(textarea);
		scrollpane.setBounds(10, 50, 400, 300);
		add(scrollpane);
		boton.setBounds(420, 10, 100, 30);
		add(boton);
		salir.setBounds(420, 50, 100, 30);
		add(salir);
		textarea.setEditable(false);
		boton.addActionListener(this);
		this.getRootPane().setDefaultButton(boton);
		salir.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se crean los flujos de entrada y salida.
		// En el flujo de salida se escribe un mensaje
		// indicando que el cliente se ha unido al Chat.
		// El HiloServidor recibe este mensaje y
		// lo reenv�a a todos los clientes conectados
		try
		{
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			String texto = "SERVIDOR> Entra en el chat... " + nombre;
			output.writeUTF(encriptacion(texto));
		}
		catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	// El m�todo main es el que lanza el cliente,
	// para ello en primer lugar se solicita el nombre o nick del
	// cliente, una vez especificado el nombre
	// se crea la conexi�n al servidor y se crear la pantalla del Chat(ClientChat)
	// lanzando su ejecuci�n (ejecutar()).
	public static void main(String[] args) throws Exception
	{
		do {
			int puerto = 44444;
			String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
			if(!nombre.trim().equals("")) 
			{
				Socket socket = null;
				try
				{
					socket = new Socket("127.0.0.1", puerto);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(), "<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}

				Cliente cliente = new Cliente(socket, nombre);
				cliente.setBounds(0,0,540,400);
				cliente.setVisible(true);
				cliente.ejecutar();
				repetir2 = false;

			}		
			else
			{
				JOptionPane.showMessageDialog(null,"El nombre est� vac�o...");
				System.out.println("El nombre est� vac�o...");
			}
		}while (repetir2);
	}
	// Cuando se pulsa el bot�n Enviar,
	// el mensaje introducido se env�a al servidor por el flujo de salida
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==boton)
		{
			String texto = nombre + "> " + mensaje.getText();
			
			try
			{				
				output.writeUTF(encriptacion(texto));
				mensaje.setText("");
			}
			catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex)
			{
				ex.printStackTrace();
			}
		}
		// Si se pulsa el bot�n Salir,
		// se env�a un mensaje indicando que el cliente abandona el chat
		// y tambi�n se env�a un * para indicar
		// al servidor que el cliente se ha cerrado
		else if(e.getSource()==salir)
		{
			String texto = "SERVIDOR> Abandona el chat... " + nombre;
			try
			{
				output.writeUTF(encriptacion(texto));
				output.writeUTF("*");
				repetir = false;
			}
			catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	// Dentro del m�todo ejecutar(), el cliente lee lo que el
	// hilo le manda y lo muestra en el textarea.
	// Esto se ejecuta en un bucle del que solo se sale
	// en el momento que el cliente pulse el bot�n Salir
	// y se modifique la variable repetir
	public void ejecutar() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		String texto = "";
		while(repetir)
		{
			try 			
			{
				texto = input.readUTF();				
				textarea.append(desencriptacion(texto)+ "\n");
			}
			catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, "Imposible conectar con	el servidor \n" + ex.getMessage(), "<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try
		{
			socket.close();
			System.exit(0);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String encriptacion(String mensaje) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{
		String resultado = "";
		
		byte[] plainBytes = mensaje.getBytes();
		byte[] keySymme = {
				0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63,
				0x72, 0x65, 0x74, 0x4b, 0x65, 0x79};// ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		
		// Crear objeto Cipher e inicializar modo encriptaci�n
		Cipher cipher = Cipher.getInstance("AES"); // Transformaci�n
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] EncryptedData = cipher.doFinal(plainBytes);
		
		System.out.println("Envia mensaje");
		System.out.println("Datos desencriptados: " + new String(plainBytes));
		System.out.println("Datos encriptado: " + new String(EncryptedData)+ "\n");
		
		resultado = new String(EncryptedData);
		
		return resultado;
	}
	
	public String desencriptacion(String mensaje) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{
		String resultado = "";		
		
		byte[] keySymme = {
				0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65,
				0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		
		byte[] plainBytes = mensaje.getBytes();
		
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE,secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(plainBytes);
			
			System.out.println("Recibe mensaje");
			System.out.println("Mensaje encriptado " + new String(plainBytes));
			System.out.println("Mensaje desencriptado " + new String(plainBytesDecrypted)+ "\n");
			
			resultado = new String(plainBytesDecrypted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resultado;
	}
}
