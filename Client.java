import java.util.*;
import java.io.*;

/**
 * Classe principal que implementa o cliente para o sistema de multicast estável.
 * Implementa a interface IStableMulticast para entrega de mensagens.
 */
public class Client implements IStableMulticast, Serializable {
    
    /**
     * Método para entregar mensagens recebidas.
     * @param msg Mensagem recebida.
     */
    public void deliver(String msg) {
        System.out.println(msg);
    }

    /**
     * Método principal que inicializa o cliente e o middleware.
     * @param args Argumentos de linha de comando, espera-se que contenha a porta.
     */
    public static void main(String[] args) {

        // Cria um novo cliente
        Client cliente = new Client();
        // Inicializa o middleware com endereço de host, porta e o cliente
        StableMulticast middleware = new StableMulticast("localhost", Integer.valueOf(args[0]), cliente);
        // Scanner para ler comandos da linha de comando
        Scanner scanner = new Scanner(System.in);

        // Loop principal para leitura de comandos do usuário
        while (true) {
            // Lê o próximo comando do usuário
            String comando = scanner.nextLine();
            
            // Verifica se o comando é para exibir a lista de clientes
            if (comando.equals("\\clientes")) {
                middleware.exibirClientes(); // Chama o método para exibir os clientes conectados
            }
            // Verifica se o comando é para exibir o buffer e os timestamps
            else if (comando.equals("\\buffer")) {
                middleware.exibirConteudoETimestamps(); // Chama o método para exibir o buffer e os timestamps
            }
            // Verifica se o comando é para sair do loop
            else if (comando.equals("\\exit")) {
                break; // Sai do loop e encerra o programa
            }
            // Caso o comando não seja um dos comandos especiais, envia a mensagem em multicast
            else {
                middleware.msend(comando); // Envia a mensagem para todos os clientes
            }
        }

        // Fecha o scanner
        scanner.close();
    }
}
