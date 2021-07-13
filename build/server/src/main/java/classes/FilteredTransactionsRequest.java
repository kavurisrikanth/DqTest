package classes;

import rest.GraphQLInputContext;

public class FilteredTransactionsRequest implements rest.IGraphQLInput {
  public double amount;

  public FilteredTransactionsRequest() {}

  public FilteredTransactionsRequest(double amount) {
    this.amount = amount;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("amount")) {
      amount = ctx.readDouble("amount");
    }
  }
}
