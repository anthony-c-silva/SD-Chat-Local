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
    public void deliver(String msg){
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
        while (true){
            String command = scanner.nextLine();
            if (command.equals("#clientes")){
                middleware.exibirClientes();
            }
            else if (command.equals("#buffer")){
                middleware.exibirConteudoETimestamps();
            }
            else if (command.equals("#exit")){
                break;
            }
            else{
                middleware.msend(command);
            }
        }
        scanner.close();
    }
}
