package payroll;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.util.ElementScanner6;

import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class Main {
  public static void main(String[] args) {
    int state = 1;
    List<Empregado> empregados = new ArrayList<Empregado>();

    int undo = 0, redo = 0, ult_op = 0;
    String id_aux = "";
    while (state != 0) {
      Scanner input = new Scanner(System.in);
      input.useLocale(Locale.ENGLISH);
      System.out.println("[1] Para cadastrar um empregado\n[2] Remover um empregado\n[3] Lancar cartao de ponto\n[4] Listar os empregados");
      System.out.println("[5] Cadastrar venda\n[6] Lancar taxa de servico\n[7] Alterar dados de um empregado\n[8] Rodar folha para hoje");
      System.out.println("[9] Alterar agenda de pagamento\n[10] Criar agenda\n[0] Sair");
      if (state != 4) { //pois o estado 4 é de listar empregados, ou seja, n é uma operação
        ult_op = state;
      }
      state = input.nextInt();
      if (state == 0) {
        break;
      }
      //copy_emp(empregados, emp_aux2);
      input = new Scanner(System.in);
      input.useLocale(Locale.ENGLISH);
      if (state == 1) {
        System.out.println("Digite o nome do empregado: ");
        String nome = input.nextLine();
        System.out.println("Digite o endereco do empregado: ");
        String endereco = input.nextLine();
        System.out.println("Digite o metodo de pagamento do empregado: ");
        String metodo = input.nextLine();
        System.out.println("Informe o tipo do empregado: \n[1] Horista\n[2] Assalariado\n[3] Comissionado");
        int tipo = input.nextInt();
        String id = UUID.randomUUID().toString();
        Empregado empregado;
        undo = 1;
        if (tipo == 1) {
          System.out.println("Digite o salario por hora do empregado " + nome + ": ");
          double salario_hora = input.nextDouble();
          Agenda agenda = new Agenda(tipo, null, DayOfWeek.FRIDAY);
          empregado = EmpregadoCrud.create(nome, id, endereco, salario_hora, 0, null, tipo, tipo, agenda, null, null, metodo);
        }
        else if (tipo == 3) {
          System.out.println("Digite o salario do empregado " + nome + ": ");
          double salario = input.nextDouble();
          System.out.println("Digite a comissao do empregado " + nome + ": ");
          double comissao = input.nextDouble();
          Agenda agenda = new Agenda(tipo, null, DayOfWeek.FRIDAY);
          empregado = EmpregadoCrud.createComissionado(nome, id, endereco, salario, salario, comissao, null, tipo, tipo, agenda, null, null, metodo);
        }
        else {
          System.out.println("Digite o salario do empregado " + nome + ": ");
          double salario = input.nextDouble();
          Agenda agenda = new Agenda(tipo, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH), null);
          empregado = EmpregadoCrud.createAssalariado(nome, id, endereco, salario, salario, null, tipo, tipo, agenda, null, null, metodo);
        }
        empregados.add(empregado);
        System.out.println("Deseja participar do sindicato?\n[1] Sim\n[2] Nao\n");
        int participarSindicato = input.nextInt();
        if (participarSindicato == 1) {
          System.out.println("Informe a taxa do sindicato que o empregado pertence: ");
          double taxa = input.nextDouble();
          UUID idSindicato = UUID.randomUUID();
          Sindicato sindicato = new Sindicato(idSindicato, id, taxa);
          empregado.setSindicato(sindicato);
        }
      }
      if (state == 2) {
        System.out.println("Informe o ID do empregado a ser removido: ");
        String idDel = input.nextLine();
        Boolean del = EmpregadoCrud.delete(empregados, idDel);
        undo = 1;
        if (del) {
          System.out.println("Empregado removido com sucesso!\n");
        }
        else {
          System.out.println("ID invalido!\n");
        }
      }
      if (state == 3) {
        System.out.println("Informe o ID do empregado para o lancamento do cartao de ponto: ");
        String idEmp = input.nextLine();
        id_aux = idEmp;
        ArrayList<Empregado> horistaLista = Empregado.getFilteredEmpregados(new FiltroHorista(), empregados);
        Empregado empregadoEdit = empEdit(idEmp, horistaLista);
        if (empregadoEdit == null) {
          System.out.println("ID invalido!");
        }
        else {
          System.out.println("Informe o horario de entrada: ");
          String entrada = input.nextLine();
          System.out.println("Informe o horario de saida");
          String saida = input.nextLine();
          empregadoEdit.getCartoes().add(new CartaoDePonto(entrada, saida));
        }    
      }
      if (state == 5) {
        System.out.println("Informe o ID do empregado para cadastrar sua venda: ");
        String idEmp = input.nextLine();
        id_aux = idEmp;
        Empregado empregadoEdit = empEdit(idEmp, empregados);
        if (empregadoEdit == null) {
          System.out.println("ID invalido!");
        }
        else {
          System.out.println("Informe o valor da venda: ");
          double valor = input.nextDouble();
          String clear = input.nextLine();
          System.out.println("Informe a data da venda: ");
          String data = input.nextLine();
          empregadoEdit.getVendas().add(new Vendas(valor, data));
        }
      }
      if (state == 6) {
        //Predicate<Empregado> empFiltro = empregado -> empregado.getSindicato() != null;
        ArrayList<Empregado> sindicatoLista = Empregado.getFilteredEmpregados(new FiltroSindicato(), empregados);

        Boolean exists = false;
        System.out.println("Informe o ID do empregado para lancar a sua taxa de servico: ");
        String idEmp = input.nextLine();
        id_aux = idEmp;
        Empregado empregadoEdit = empEdit(idEmp, empregados);
        if (empregadoEdit == null) {
          System.out.println("ID invalido!");
        }
        else {
          System.out.println("Informe o valor da taxa: ");
          double valor = input.nextDouble();
          String clear = input.nextLine();
          System.out.println("Informe o nome do servico: ");
          String servico = input.nextLine();
          empregadoEdit.getSindicato().getTaxa().add(new Taxa(valor, servico));
        }
      }
      if (state == 7) {
        System.out.println("Informe o ID do empregado a ser alterado: ");
        String idEmp = input.nextLine();
        id_aux = idEmp;
        Empregado empregadoEdit = empEdit(idEmp, empregados);
        if (empregadoEdit == null) {
          System.out.println("ID invalido!");
        }
        else {
          int menuAlteracao = 1;
          undo = 1;
          while (menuAlteracao != 0) {
            Scanner in = new Scanner(System.in);
            in.useLocale(Locale.ENGLISH);
            System.out.println("[1] Alterar nome\n[2] Alterar endereco\n[3] Alterar tipo\n[4] Alterar metodo de pagamento");
            System.out.println("[5] Alterar participacao no sindicato\n[6] Alterar indentificacao no sindicato\n[7] Alterar taxa sindical");
            System.out.println("[0] Sair");
            menuAlteracao = in.nextInt();
            String clear = in.nextLine();
            if (menuAlteracao == 0) {
              break;
            }
            if (menuAlteracao == 1) {
              
              System.out.println("Informe o novo nome: ");
              String newName = in.nextLine();
              empregadoEdit.setName(newName);
            }
            else if (menuAlteracao == 2) {
              System.out.println("Informe o novo endereco: ");
              String newAddress = in.nextLine();
              empregadoEdit.setAddress(newAddress);
            }
            else if (menuAlteracao == 3) {
              System.out.println("Informe o novo tipo: \n[1] Horista\n[2] Assalariado\n[3] Comissionado\n");
              int tipo = in.nextInt();
              clear = in.nextLine();
              Empregado aux = empregadoEdit;
              empregados.remove(empregadoEdit);
              if (tipo == 1) {
                System.out.println("Informe o salario por hora: ");
                double salario_hora = in.nextDouble();
                clear = in.nextLine();
                empregadoEdit = EmpregadoCrud.create(aux.getName(), aux.getId(), aux.getAddress(), salario_hora, 0, aux.getSindicato(), tipo, tipo, aux.getAgenda(), aux.getCartoes(), aux.getVendas(), aux.getMetodo());
              }
              else if (tipo == 2) {
                System.out.println("Digite o salario do empregado: ");
                double salario = input.nextDouble();
                clear = in.nextLine();
                empregadoEdit = EmpregadoCrud.createAssalariado(aux.getName(), aux.getId(), aux.getAddress(), salario, salario, aux.getSindicato(), tipo, tipo, aux.getAgenda(), aux.getCartoes(), aux.getVendas(), aux.getMetodo());
              }
              else if (tipo == 3) {
                System.out.println("Digite o salario do empregado: ");
                double salario = input.nextDouble();
                System.out.println("Digite a comissao do empregado: ");
                double comissao = input.nextDouble();
                empregadoEdit = EmpregadoCrud.createComissionado(aux.getName(), aux.getId(), aux.getAddress(), salario, salario, comissao, aux.getSindicato(), tipo, tipo, aux.getAgenda(), aux.getCartoes(), aux.getVendas(), aux.getMetodo());
              }
              empregados.add(empregadoEdit);
            }
            else if (menuAlteracao == 4) {
              System.out.println("Digite o novo metodo de pagamento");
              String newMethod = in.nextLine();
              empregadoEdit.setMetodo(newMethod);
            }
            else if (menuAlteracao == 5) {
              if (empregadoEdit.getSindicato() != null) {
                System.out.println("Voce deseja retirar o empregado do sindicato?\n[1] Sim\n[2] Nao");
                int sind = in.nextInt();
                if (sind == 1) {
                  empregadoEdit.setSindicato(null);
                }
              }
              else {
                System.out.println("Voce deseja adicionar o empregado ao sindicato?\n[1] Sim\n[2] Nao");
                int sind = in.nextInt();
                if (sind == 1) {
                  System.out.println("Informe a taxa do sindicato que o empregado pertence: ");
                  double taxa = input.nextDouble();
                  UUID idSindicato = UUID.randomUUID();
                  Sindicato sindicato = new Sindicato(idSindicato, empregadoEdit.getId(), taxa);
                  empregadoEdit.setSindicato(sindicato);
                }
              }
            }
            else if (menuAlteracao == 6) {
              if (empregadoEdit.getSindicato() == null) {
                System.out.println("O empregado não pertence a um sindicato!");
              }
              else {
                String idEmpSind = UUID.randomUUID().toString();
                empregadoEdit.getSindicato().setIdEmp(idEmpSind);
                System.out.println("ID alterado para: " + idEmpSind);
              }
            }
            else if (menuAlteracao == 7) {
              if (empregadoEdit.getSindicato() == null) {
                System.out.println("O empregado não pertence a um sindicato!");
              }
              else {
                System.out.println("Digite a nova taxa para o sindicato: ");
                double tx = input.nextDouble();
                empregadoEdit.getSindicato().setTaxaSindicato(tx);
              }
            }
          }
        }
      }
      if (state == 4) {
        for (Empregado e : empregados) {
          System.out.println(e + "\n");
        }
      }
      if (state == 8) { 
        LocalDate date = LocalDate.now();  
        DayOfWeek dayOfWeek = DayOfWeek.from(date);
        ArrayList<Empregado> ans = new ArrayList<Empregado>();
        undo = 1;
        for (Empregado e : empregados) {
          //[1] Semanal [2] Mensal [3] Bi-semanal
          if (e.getTipoPagamento() == 1) {
            if (dayOfWeek == e.getAgenda().getDayOfWeek()) {
              ans.add(e);
            }
          }
          else if (e.getTipoPagamento() == 2) {
            if (date.getDayOfMonth() == e.getAgenda().getDayOfMonth()) {
              ans.add(e);
            }
          }
          else if (e.getTipoPagamento() == 3) {
            //pagando por padrao nos respectivos dias das 2 primeiras semanas
            if (dayOfWeek == e.getAgenda().getDayOfWeek() && date.getDayOfMonth() <= 14) {
              ans.add(e);
            }
          }
          
        }
        pay_emp(ans);
      }
      
      if (state == 9) {
        System.out.println("Informe o ID do empregado a ser alterada a agenda de pagamento:");
        Scanner in = new Scanner(System.in);
        String id = in.nextLine();
        boolean exists = false;
        for (Empregado e : empregados) {
          if (e.getId().equals(id)) {
            exists = true;
            System.out.println("Digite qual tipo de agenda de pagamento sera escolhida para alteracao");
            System.out.println("[1] Semanal\n[2] Mensal\n[3] Bi-semanal\n[4] Criar nova agenda");
            Scanner in2 = new Scanner(System.in);
            int payment_type = in2.nextInt();
            e.setTipoPagamento(payment_type);
            switch (payment_type) {
              case 1:
                Agenda agenda1 = new Agenda(payment_type, 0, DayOfWeek.FRIDAY);
                e.setAgenda(agenda1);
              case 2:
                Agenda agenda2 = new Agenda(payment_type, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH), null);
                e.setAgenda(agenda2);
              case 3:
                Agenda agenda3 = new Agenda(payment_type, 0, DayOfWeek.FRIDAY);
                e.setAgenda(agenda3);
            }
          }
        }
        if (!exists) {
          System.out.println("Informe um ID valido!");
        }
      }
      if (state == 10) {
        Scanner in = new Scanner(System.in);
        System.out.println("Digite o ID do funcionário a atribuir a agenda");
        String id = in.nextLine();
        Empregado empAtual = null;
        for (Empregado e : empregados) {
          if (e.getId().equals(id)) {
            empAtual = e;
          }
        }
        System.out.println("Digite qual tipo de agenda de pagamento sera escolhida:");
        System.out.println("[1] Semanal\n[2] Mensal\n[3] Bi-semanal");
        Scanner in2 = new Scanner(System.in);
        int tipo_agenda = in2.nextInt();
        switch (tipo_agenda) {
          case 1:
            System.out.println("Informe o dia da semana o qual o empregado ira receber:");
            System.out.println("[0] Domingo\n[1] Segunda\n[2] Terca\n[3] Quarta\n[4] Quinta\n[5] Sexta\n[6] Sabado");
            int dayOfWeek = in2.nextInt();
            DayOfWeek d = null;
            if (dayOfWeek == 0) {
              d = DayOfWeek.SUNDAY;
            }
            else if (dayOfWeek == 1) {
              d = DayOfWeek.MONDAY;
            }
            else if (dayOfWeek == 2) {
              d = DayOfWeek.TUESDAY;
            }
            else if (dayOfWeek == 3) {
              d = DayOfWeek.WEDNESDAY;
            }
            else if (dayOfWeek == 4) {
              d = DayOfWeek.THURSDAY;
            }
            else if (dayOfWeek == 5) {
              d = DayOfWeek.FRIDAY;
            }
            else if (dayOfWeek == 6) {
              d = DayOfWeek.SATURDAY;
            }
            Agenda agenda1 = new Agenda(tipo_agenda, null, d);
            empAtual.setAgenda(agenda1);
            break;
          case 2:
            System.out.println("Informe o dia do mes que o empregado ira receber:");
            int dayOfMonth = in2.nextInt();
            Agenda agenda2 = new Agenda(tipo_agenda, dayOfMonth, null);
            empAtual.setAgenda(agenda2);
            break;
          case 3:
            System.out.println("Informe o dia da semana o qual o empregado ira receber:");
            System.out.println("[0] Domingo\n[1] Segunda\n[2] Terca\n[3] Quarta\n[4] Quinta\n[5] Sexta\n[6] Sabado");
            int day = in2.nextInt();
            DayOfWeek d2 = null;
            if (day == 0) {
              d2 = DayOfWeek.SUNDAY;
            }
            else if (day == 1) {
              d2 = DayOfWeek.MONDAY;
            }
            else if (day == 2) {
              d2 = DayOfWeek.TUESDAY;
            }
            else if (day == 3) {
              d2 = DayOfWeek.WEDNESDAY;
            }
            else if (day == 4) {
              d2 = DayOfWeek.THURSDAY;
            }
            else if (day == 5) {
              d2 = DayOfWeek.FRIDAY;
            }
            else if (day == 6) {
              d2 = DayOfWeek.SATURDAY;
            }
            Agenda agenda3 = new Agenda(tipo_agenda, null, d2);
            empAtual.setAgenda(agenda3);
            break;
        }
      }
    }
  }
  public static Empregado empEdit(String id, List<Empregado> empregados) {
    for (Empregado empregado : empregados) {
      if (empregado.getId().equals(id)) {
        return empregado;
      }
    }
    return null;
  }
  public static void pay_emp(List<Empregado> emp) {
    System.out.println("Lista de funcionarios: ");
    
    for (Empregado e : emp) {
      System.out.println("--------------------------------");
      System.out.println("Nome: " + e.getName());
      if (e.getType() == 1) {
        Horista h = (Horista) e;
        System.out.println("Salario: " + h.getSalario());
      }
      else if (e.getType() == 2) {
        Assalariado a = (Assalariado) e;
        System.out.println("Salario: " + a.getSalario());
      }
      else if (e.getType() == 3) {
        Comissionado c = (Comissionado) e;
        System.out.println("Salario: " + c.getSalario());
      }
      System.out.println("Metodo de Pagamento: " + e.getMetodo());
      System.out.print("--------------------------------\n");
    }
  }
}