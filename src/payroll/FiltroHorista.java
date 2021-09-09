package payroll;

import payroll.Horista;
import payroll.Empregado;

public class FiltroHorista implements FiltroEmpregado{
  @Override
  public boolean instanciaEmpregado(Empregado emp) {
    return emp instanceof Horista;
  }
}
