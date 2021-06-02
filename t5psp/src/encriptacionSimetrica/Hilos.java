package encriptacionSimetrica;
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

public class Hilos extends Thread
{
	DataInputStream input;
	Socket socket;
	boolean fin = false;
	
	public Hilos(Socket socket)
	{
		this.socket = socket;
		try
		{
			input = new DataInputStream(socket.getInputStream());
		}
		
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}
	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{
		Servidor.mensaje.setText("Número de conexiones actuales: " + Servidor.ACTUALES);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while(!fin)
		{
			String cadena = "";
			try
			{
				cadena = input.readUTF();
				String cadenaDesencriptada = desencriptacion(cadena);				
				
				if(cadena.trim().equals("*"))
				{
					Servidor.ACTUALES--;
					Servidor.mensaje.setText("Número de conexiones actuales: "
							+ Servidor.ACTUALES);
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					Servidor.textarea.append(cadenaDesencriptada + "\n");
					//texto = Servidor.textarea.getText();
					EnviarMensajes(cadena);
				}
			}
			
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin=true;
			}
		}
	}
	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket
	private void EnviarMensajes(String texto)
	{
		for(int i=0; i<Servidor.CONEXIONES; i++)
		{
			Socket socket = Servidor.tabla[i];
			try
			{
				DataOutputStream fsalida = new
						DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			}
			catch (IOException  e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
	public String desencriptacion(String mensaje) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException 
	{
		String resultado = "";		
		
		byte[] keySymme = {
				0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65,
				0x74, 0x4b, 0x65, 0x79
		}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, "AES");
		
		byte[] plainBytes = mensaje.getBytes();
		
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE,secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(plainBytes);
			
			System.out.println("Recibe mensaje");
			System.out.println("Mensaje encriptado " + new	String(plainBytes));
			System.out.println("Mensaje desencriptado " + new	String(plainBytesDecrypted)+ "\n");
			System.out.println("Envia mensaje");
			System.out.println("Mensaje encriptado " + new	String(plainBytes)+ "\n");
			resultado = new String(plainBytesDecrypted);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resultado;
	}
}
