package payroll;

import payroll.Empregado;

public class FiltroSindicato implements FiltroEmpregado{
  @Override
  public boolean instanciaEmpregado(Empregado emp) {
    return (emp.getSindicato() != null);
  }
}
