
import java.util.*;

enum StatusPedido {
    PENDENTE, PROCESSANDO, ENVIADO, ENTREGUE, CANCELADO
}

enum Categoria {
    ELETRONICO, ROUPA, ALIMENTO
}

interface Processavel {
    void processarPedido() throws PedidoInvalidoException;
    double calcularValorFinal();
}

interface Entregavel {
    double calcularFrete();
}

class PedidoInvalidoException extends Exception {
    PedidoInvalidoException(String mensagem) {
        super(mensagem);
    }
}

class ValorInvalidoException extends Exception {
    ValorInvalidoException() {
        super("O valor deve ser maior que zero.");
    }
}

abstract class Pedido implements Processavel {
    final int id;
    final String nomeCliente;
    final double valor;
    final Categoria categoria;
    StatusPedido status = StatusPedido.PENDENTE;

    Pedido(int id, String nomeCliente, double valor, Categoria categoria)
            throws ValorInvalidoException {
        if (valor <= 0 || !Double.isFinite(valor))
            throw new ValorInvalidoException();

        this.id = id;
        this.nomeCliente = nomeCliente;
        this.valor = valor;
        this.categoria = categoria;
    }

    void alterarStatus(StatusPedido novo) throws PedidoInvalidoException {
        if (status == StatusPedido.ENTREGUE)
            throw new PedidoInvalidoException(
                "Pedido entregue não pode mudar de status."
            );
        status = novo;
    }

    public void processarPedido() throws PedidoInvalidoException {
        if (status == StatusPedido.CANCELADO ||
            status == StatusPedido.ENTREGUE)
            throw new PedidoInvalidoException(
                "Pedido " + id + " não pode ser processado: " + status
            );

        status = StatusPedido.PROCESSANDO;
    }

    abstract double desconto();

    public double calcularValorFinal() {
        double frete = this instanceof Entregavel
                ? ((Entregavel) this).calcularFrete() : 0;
        return valor - desconto() + frete;
    }

    public String toString() {
        return String.format(
            "#%d | %s | %s | %s | Total: R$ %.2f",
            id, nomeCliente, categoria, status, calcularValorFinal()
        );
    }
}

class PedidoEletronico extends Pedido implements Entregavel {
    PedidoEletronico(int id, String nome, double valor)
            throws ValorInvalidoException {
        super(id, nome, valor, Categoria.ELETRONICO);
    }

    double desconto() {
        return valor > 100 ? valor * 0.10 : 0;
    }

    public double calcularFrete() {
        return 20 + valor * 0.10;
    }
}

class PedidoRoupa extends Pedido implements Entregavel {
    PedidoRoupa(int id, String nome, double valor)
            throws ValorInvalidoException {
        super(id, nome, valor, Categoria.ROUPA);
    }

    double desconto() {
        return valor > 200 ? valor * 0.20 : 0;
    }

    public double calcularFrete() {
        return 10 + valor * 0.05;
    }
}

class PedidoAlimento extends Pedido {
    PedidoAlimento(int id, String nome, double valor)
            throws ValorInvalidoException {
        super(id, nome, valor, Categoria.ALIMENTO);
    }

    double desconto() {
        return 0;
    }

    public double calcularValorFinal() {
        return valor + 5;
    }
}

public class Main {
    static final Scanner scanner = new Scanner(System.in);
    static final List<Pedido> pedidos = new ArrayList<>();
    static int proximoId = 1;

    static String ler(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    static int inteiro(String mensagem) {
        return Integer.parseInt(ler(mensagem));
    }

    static double decimal(String mensagem) {
        return Double.parseDouble(ler(mensagem).replace(',', '.'));
    }

    static Pedido buscar(int id) {
        for (Pedido p : pedidos)
            if (p.id == id) return p;

        throw new IllegalArgumentException("Pedido não encontrado.");
    }

    static void listar(boolean apenasEntregaveis) {
        boolean encontrou = false;

        for (Pedido p : pedidos) {
            if (p.status != StatusPedido.CANCELADO &&
                (!apenasEntregaveis || p instanceof Entregavel)) {
                System.out.println(p);
                encontrou = true;
            }
        }

        if (!encontrou)
            System.out.println("Nenhum pedido encontrado.");
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println(
                "\n1-Cadastrar  2-Alterar status  3-Processar todos  4-Remover cancelados"
            );
            System.out.println(
                "5-Listar pedidos  6-Listar entregáveis  7-Sair"
            );

            try {
                int opcao = inteiro("Opção: ");

                switch (opcao) {
                    case 1: {
                        int tipo = inteiro(
                            "Tipo (1-Eletrônico, 2-Roupa, 3-Alimento): "
                        );

                        if (tipo < 1 || tipo > 3)
                            throw new IllegalArgumentException("Tipo inválido.");

                        String nome = ler("Cliente: ");

                        if (nome.isEmpty())
                            throw new IllegalArgumentException(
                                "Informe o nome do cliente."
                            );

                        double valor = decimal("Valor: R$ ");

                        Pedido p = tipo == 1
                                ? new PedidoEletronico(proximoId, nome, valor)
                                : tipo == 2
                                ? new PedidoRoupa(proximoId, nome, valor)
                                : new PedidoAlimento(proximoId, nome, valor);

                        pedidos.add(p);
                        proximoId++;

                        System.out.println("Cadastrado: " + p);
                        break;
                    }

                    case 2: {
                        Pedido p = buscar(inteiro("ID do pedido: "));
                        StatusPedido[] status = StatusPedido.values();

                        for (int i = 0; i < status.length; i++)
                            System.out.println((i + 1) + "-" + status[i]);

                        int indice = inteiro("Novo status: ") - 1;

                        if (indice < 0 || indice >= status.length)
                            throw new IllegalArgumentException("Status inválido.");

                        p.alterarStatus(status[indice]);
                        System.out.println("Status atualizado: " + p);
                        break;
                    }

                    case 3: {
                        Iterator<Pedido> it = pedidos.iterator();

                        while (it.hasNext()) {
                            Pedido p = it.next();

                            try {
                                p.processarPedido();
                                System.out.println("Processado: " + p);
                            } catch (PedidoInvalidoException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    }

                    case 4: {
                        int removidos = 0;
                        Iterator<Pedido> it = pedidos.iterator();

                        while (it.hasNext()) {
                            if (it.next().status == StatusPedido.CANCELADO) {
                                it.remove();
                                removidos++;
                            }
                        }

                        System.out.println("Pedidos removidos: " + removidos);
                        break;
                    }

                    case 5:
                        listar(false);
                        break;

                    case 6:
                        listar(true);
                        break;

                    case 7:
                        System.out.println("Programa encerrado.");
                        return;

                    default:
                        System.out.println("Opção inválida.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.");
            } catch (ValorInvalidoException | PedidoInvalidoException |
                     IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }
}
