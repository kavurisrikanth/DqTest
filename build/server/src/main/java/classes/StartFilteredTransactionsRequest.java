package classes;

import rest.GraphQLInputContext;

public class StartFilteredTransactionsRequest implements rest.IGraphQLInput {
  public double amount;

  public StartFilteredTransactionsRequest() {}

  public StartFilteredTransactionsRequest(double amount) {
    this.amount = amount;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("amount")) {
      amount = ctx.readDouble("amount");
    }
  }
}
