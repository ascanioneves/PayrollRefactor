package payroll;

import payroll.Comissionado;
import payroll.Empregado;

public class FiltroComissionado implements FiltroEmpregado{
  @Override
  public boolean instanciaEmpregado(Empregado emp) {
    return emp instanceof Comissionado;
  }
}
