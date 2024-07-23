import java.util.*;
import java.net.*;
import java.io.*;


/**
 * Representa uma mensagem trocada entre clientes em um sistema de multicast.
 */
class Message implements Serializable {
    private Integer[] timestamp;   // Array que guarda os timestamps da mensagem
    private String textoMensagem;  // Conteúdo da mensagem
    private String comando;        // Comando associado à mensagem
    private ClientInfo cliente;    // Informações do cliente que enviou a mensagem
    private ArrayList<ClientInfo> listaClientes; // Lista de clientes conectados

    /**
     * Construtor para criar uma nova mensagem.
     * @param timestamp Array de timestamps
     * @param textoMensagem Conteúdo da mensagem
     * @param cliente Informações do cliente
     * @param comando Comando associado
     */
    public Message(Integer[] timestamp, String textoMensagem, ClientInfo cliente, String comando) {
        this.timestamp = timestamp;
        this.textoMensagem = textoMensagem;
        this.cliente = cliente;
        this.comando = comando;
    }

     /**
     * Retorna o conteúdo da mensagem.
     * @return Conteúdo da mensagem
     */
    public String message(){
        return textoMensagem;
    }

     /**
     * Retorna as informações do cliente que enviou a mensagem.
     * @return Informações do cliente
     */
    public ClientInfo cliente(){
        return cliente;
    }

    /**
     * Retorna o array de timestamps da mensagem.
     * @return Array de timestamps
     */
    public Integer[] timestamp(){
        return timestamp;
    }

     /**
     * Retorna o comando associado à mensagem.
     * @return Comando
     */
    public String command(){
        return comando;
    }

    /**
     * Define a lista de clientes conectados.
     * @param listaClientes Lista de clientes
     */
    public void setClientList(ArrayList<ClientInfo> clientes){
        this.listaClientes = clientes;
    }
    
    /**
     * Retorna a lista de clientes conectados.
     * @return Lista de clientes
     */
    public ArrayList<ClientInfo> getClientList(){
        return this.listaClientes;
    }
}

class ClientInfo implements Serializable {
    private String nome;
    private Integer identificador;
    private InetAddress enderecoIP;
    private Integer porta;
    private IStableMulticast cliente;

    /**
     * Construtor da classe ClientInfo.
     * @param enderecoIP endereço IP do cliente
     * @param porta porta do cliente
     * @param nome nome do cliente
     * @param cliente instância de IStableMulticast associada ao cliente
     */
    public ClientInfo(InetAddress enderecoIP, Integer porta, String nome, IStableMulticast cliente) {
        this.enderecoIP = enderecoIP;
        this.porta = porta;
        this.cliente = cliente;
        this.nome = nome;
        this.identificador = -1; // Inicializa o ID como -1 para indicar que ainda não foi definido
    }

     /**
     * Obtém a instância de IStableMulticast associada ao cliente.
     * @return instância de IStableMulticast
     */
    public IStableMulticast getClient(){
        return cliente;
    }

     /**
     * Obtém o endereço IP do cliente.
     * @return endereço IP do cliente
     */
    public InetAddress getIP(){
        return enderecoIP;
    }

     /**
     * Obtém a porta do cliente.
     * @return porta do cliente
     */
    public Integer getPort(){
        return porta;
    }

     /**
     * Obtém o nome do cliente.
     * @return nome do cliente
     */
    public String getName(){
        return nome;
    }

     /**
     * Obtém o ID do cliente.
     * @return ID do cliente
     */
    public Integer getID(){
        return identificador;
    }

     /**
     * Define o ID do cliente.
     * @param id novo ID do cliente
     */
    public void setID(Integer id){
        this.identificador = id;
    }
}


public class StableMulticast implements Serializable{

    // Informações dos outros clientes
    private ArrayList<ClientInfo> clientes;

    // Buffer e timestamps
    private List<Message> buffer;
    private Integer[][] MCi;

    // Informações do cliente
    private ClientInfo client;

    // Unicast
    private String ip_unicast;
    private Integer porta_unicast;
    private InetAddress unicast;
    private DatagramSocket socket_unicast;

    // Multicast
    private String ip = "224.0.5.1";
    private Integer porta = 1236;
    private InetAddress multicast;
    private MulticastSocket socket;

    // Cores
    public final String COLOR_RESET = "\u001B[0m";
    public final String COLOR_PURPLE = "\u001B[35m";
    public final String COLOR_CYAN = "\u001B[36m";

    // Tamanho do grupo
    final private Integer tamanho_grupo = 3;

    private Scanner input;


    public StableMulticast(String enderecoIP, Integer porta, IStableMulticast cliente) {

        // Inicializa o endereço IP e a porta para comunicação unicast
        this.ip_unicast = enderecoIP;
        this.porta_unicast = porta;
    
        // Configura os arrays necessários
        this.clientes = new ArrayList<>();
        this.MCi = new Integer[tamanho_grupo][tamanho_grupo];

        for (Integer[] linha : this.MCi) {
            Arrays.fill(linha, -1);
        }
        this.buffer = new ArrayList<>();        
    
        // Coleta o nome do cliente
        this.input = new Scanner(System.in);
        System.out.println("Por favor, insira seu nome:");
        String nomeCliente = input.nextLine();
    
        // Inicializa os sockets
        try {
            this.multicast = InetAddress.getByName(this.ip);
            this.socket = new MulticastSocket(this.porta);
    
            this.unicast = InetAddress.getByName(this.ip_unicast);
            this.socket_unicast = new DatagramSocket(this.porta_unicast, this.unicast);
    
            // Obtém a interface de rede apropriada
            NetworkInterface redeInterface = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.supportsMulticast() && !iface.isLoopback() && iface.isUp()) {
                    redeInterface = iface;
                    break;
                }
            }
    
            if (redeInterface == null) {
                System.err.println("Nenhuma interface de rede adequada foi encontrada!");
                return;
            }
    
            socket.joinGroup(new InetSocketAddress(this.multicast, this.porta), redeInterface);
            socket.setReuseAddress(true);           
        } catch (Exception e) {
            System.err.println("Erro ao criar o socket!");
            e.printStackTrace();
            return;
        }
    
        // Inicializa os listeners para os sockets
        listening(this.socket); 
        listening(this.socket_unicast);
    
        // Cria e envia a mensagem de adesão em multicast
        this.client = new ClientInfo(unicast, porta_unicast, nomeCliente, cliente);
        Message mensagemJoin = new Message(null, "", this.client, "join");
        enviarMensagem(mensagemJoin, this.socket, this.multicast, this.porta);
    }
    

    /**
     * Exibe o conteúdo do buffer e a matriz de timestamps de maneira formatada.
     */
    public void exibirConteudoETimestamps() {
        // Coleta as mensagens do buffer em uma lista temporária
        List<String> mensagens = new ArrayList<>();
        for (Message mensagem : buffer) {
            mensagens.add(mensagem.message());
        }
        
        // Exibe o conteúdo das mensagens do buffer
        System.out.println("Conteudo do Buffer: " + mensagens);
        
        // Exibe a matriz de timestamps
        System.out.println("Matriz de Timestamps:");
        for (int linha = 0; linha < tamanho_grupo; linha++) {
            // Imprime a abertura do colchete para a linha
            if (linha == 0) {
                System.out.print("[");
            } else {
                System.out.print(" [");
            }
            
            // Imprime os valores da linha atual da matriz de timestamps
            for (int coluna = 0; coluna < tamanho_grupo - 1; coluna++) {
                System.out.print(MCi[linha][coluna] + ", ");
            }
            
            // Imprime o último valor da linha e fecha o colchete
            System.out.print(MCi[linha][tamanho_grupo - 1] + "]");
            System.out.println(); // Nova linha para a próxima linha da matriz
        }
    }

    /**
     * Exibe os nomes de todos os clientes armazenados na lista.
     */
    public void exibirClientes() {
        // Itera sobre a lista de clientes e imprime o nome de cada um
        for (ClientInfo cliente : this.clientes) {
            System.out.println(cliente.getName());
        }
    }

    /**
     * Obtém o menor valor de timestamp para um remetente específico.
     * @param remetente ID do cliente que enviou a mensagem
     * @return o menor timestamp registrado para o remetente
     */
    private int obterTimestampMinimo(Integer remetente) {
        int indice = 0;
        int timestampMinimo = Integer.MAX_VALUE; // Utiliza o valor máximo possível como inicial
        // Percorre todos os timestamps para encontrar o menor valor
        while (indice < tamanho_grupo) {
            if (MCi[indice][remetente] < timestampMinimo) {
                timestampMinimo = MCi[indice][remetente];
            }
            indice++;
        }
        return timestampMinimo;
    }

    /**
     * Verifica e remove mensagens do buffer que não atendem aos critérios de timestamp.
     */
    private void verificarEliminarBuffer() {
        // Verifica se o buffer contém mensagens
        if (!buffer.isEmpty()) {
            int indice = 0;
            while (indice < buffer.size()) {
                // Obtém a mensagem atual do buffer
                Message mensagemAtual = buffer.get(indice);
                // Verifica se o timestamp da mensagem atende ao critério mínimo
                if (mensagemAtual.timestamp()[mensagemAtual.cliente().getID()] <= obterTimestampMinimo(mensagemAtual.cliente().getID())) {
                    // Remove a mensagem do buffer
                    buffer.remove(mensagemAtual);
                    System.out.println("Mensagem removida do buffer: " + mensagemAtual.message());
                    indice--; // Ajusta o índice após remoção
                }
                indice++;
            }
        }
    }

    /**
     * Converte um objeto em um array de bytes para transmissão.
     * @param objeto o objeto a ser serializado
     * @return o array de bytes representando o objeto
     */
    private byte[] converterParaBytes(Object objeto) {
        // Cria um fluxo de saída de bytes para armazenar a serialização
        try (ByteArrayOutputStream fluxoSaida = new ByteArrayOutputStream();
            ObjectOutputStream fluxoObjetos = new ObjectOutputStream(fluxoSaida)) {

            // Serializa o objeto e grava no fluxo de bytes
            fluxoObjetos.writeObject(objeto);
            // Retorna o array de bytes resultante da serialização
            return fluxoSaida.toByteArray();
        } catch (Exception ex) {
            System.err.println("Falha ao converter o objeto para bytes!");
            ex.printStackTrace();
        }
        return new byte[0]; // Retorna um array vazio em caso de erro
    }


    /**
     * Envia uma mensagem através de um socket especificado.
     * @param mensagem objeto da mensagem a ser enviada
     * @param socket socket utilizado para envio
     * @param endereco IP de destino
     * @param porta porta de destino
     */
    private void enviarMensagem(Message mensagem, DatagramSocket socket, InetAddress endereco, Integer porta) {
        try {
            // Serializa o objeto da mensagem
            byte[] dadosParaEnvio = converterParaBytes(mensagem);
            // Cria um pacote de dados com o endereço e a porta de destino
            DatagramPacket pacoteEnvio = new DatagramPacket(dadosParaEnvio, dadosParaEnvio.length, endereco, porta);
            // Envia o pacote através do socket
            socket.send(pacoteEnvio);
        } catch (Exception ex) {
            System.err.println("Falha ao enviar a mensagem via unicast!");
            ex.printStackTrace();
        }
    }

    /**
     * Converte um array de bytes em um objeto Java.
     * @param byteArray o array de bytes a ser desserializado
     * @return o objeto desserializado, ou null se ocorrer um erro
     */
    public Object converteParaObject(byte[] byteArray) {
        // Utiliza ByteArrayInputStream para ler os bytes do array
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            // Retorna o objeto desserializado
            return objectStream.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Falha na desserialização do objeto.");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Recebe uma mensagem através de um socket especificado.
     * @param socket socket utilizado para recepção
     * @return o objeto da mensagem recebida
     */
    private Message receberMensagem(DatagramSocket socket) {
        byte[] dadosRecebidos = new byte[1024];
        DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
        try {
            // Recebe o pacote de dados através do socket
            socket.receive(pacoteRecebido);
            // Converte os dados recebidos em um objeto de mensagem
            return (Message) converteParaObject(pacoteRecebido.getData());
        } catch (Exception ex) {
            System.err.println("Falha ao receber a mensagem!");
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Método para ouvir mensagens em um socket.
     * @param socket socket a ser ouvido
     */
    private synchronized void listening(DatagramSocket skt) {
        Thread udpThread = new Thread(() -> {
            try {
                // Loop para receber mensagens.
                while (!Thread.currentThread().isInterrupted()) {
                    Message sms = receberMensagem(skt);
                    switch (sms.command()) {
                        case "msg":
                            this.buffer.add(sms);
                            if (sms.cliente().getID().intValue() != this.client.getID().intValue()) {
                              
                                this.MCi[sms.cliente().getID()] = sms.timestamp();  // Essa linha deveria estar dentro do if no codigo do trab
                                this.MCi[this.client.getID()][sms.cliente().getID()]++;
                            }
                            this.client.getClient().deliver(sms.message());

                            verificarEliminarBuffer();
                            mostrarBufferETimestamps();

                            break;
                        case "join":
                            sms.cliente().setID(this.clientes.size());
                            clientes.add(sms.cliente());
                            this.client.getClient().deliver(COLOR_CYAN + "Bem vindo(a) " + sms.cliente().getName() + " ao chat!" + COLOR_RESET);

                            Message hello = new Message(null, "", this.client, "hello");
                            hello.setClientList(clientes);
                            enviarMensagem(hello, this.socket_unicast, sms.cliente().getIP(), sms.cliente().getPort());
                            break;
                        case "hello":
                            this.clientes = sms.getClientList();
                            this.client.setID(this.clientes.size() - 1);
                            this.MCi[this.client.getID()][this.client.getID()] = 0;
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao receber mensagens!");
                e.printStackTrace();
            }
        });
        udpThread.start();
    }

    /**
     * Apresenta o conteúdo do buffer e os timestamps de maneira organizada.
     */
    private void mostrarBufferETimestamps() {
        System.out.println("Conteudo do Buffer:");
        for (Message mensagem : buffer) {
            System.out.println("  " + mensagem.cliente().getName() + ": " + mensagem.message());
        }

        System.out.println("Matriz de Timestamps:");
        for (Integer[] linha : MCi) {
            System.out.print("  ");
            for (Integer timestamp : linha) {
                System.out.print(String.format("%3d ", timestamp != null ? timestamp : -1));
            }
            System.out.println();
        }
    }


/**
 * Envia uma mensagem para múltiplos destinatários via multicast.
 * @param conteudoMensagem o conteúdo da mensagem a ser enviada
 */
public synchronized void msend(String conteudoMensagem) {
    // Formata a mensagem com a cor e o nome do cliente
    String mensagemFormatada = this.COLOR_PURPLE + this.client.getName() + ": " + this.COLOR_RESET + conteudoMensagem;
    // Cria um novo objeto Message com o timestamp atual e o conteúdo formatado
    Message mensagem = new Message(Arrays.stream(MCi[this.client.getID()]).toArray(Integer[]::new), mensagemFormatada, this.client, "msg");
    // Incrementa o timestamp do cliente atual
    this.MCi[this.client.getID()][this.client.getID()]++;

    // Pergunta ao usuário se deseja enviar a mensagem para todos
    System.out.println("Deseja enviar para todos? 's' para sim ou 'n' para nao.");
    String resposta = this.input.nextLine();
    
    switch (resposta) {
        case "s":
            // Envia a mensagem para todos os clientes
            for (ClientInfo cliente : this.clientes) {
                enviarMensagem(mensagem, this.socket_unicast, cliente.getIP(), cliente.getPort());
            }
            break;
        case "n":
            // Envia a mensagem para clientes selecionados individualmente
            for (ClientInfo cliente : this.clientes) {
                if (!cliente.getID().equals(this.client.getID())) {
                    System.out.println("Pressione Enter para enviar para: " + COLOR_CYAN + cliente.getName() + COLOR_RESET);
                    this.input.nextLine();  // Espera o usuário pressionar Enter
                }
                enviarMensagem(mensagem, this.socket_unicast, cliente.getIP(), cliente.getPort());
            }
            break;
        default:
            System.err.println("Comando nao reconhecido. Tente novamente.");
            break;
    }
}
}