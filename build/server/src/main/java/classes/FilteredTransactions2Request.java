package classes;

import rest.GraphQLInputContext;

public class FilteredTransactions2Request implements rest.IGraphQLInput {
  public double amount;

  public FilteredTransactions2Request() {}

  public FilteredTransactions2Request(double amount) {
    this.amount = amount;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("amount")) {
      amount = ctx.readDouble("amount");
    }
  }
}
