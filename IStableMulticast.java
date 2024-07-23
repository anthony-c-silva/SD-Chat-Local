/**
 * Interface que deve ser implementada por todo usuário do pacote StableMulticast.
 * Define o método deliver para entrega de mensagens.
 */
interface IStableMulticast{
   public void deliver(String msg);
}