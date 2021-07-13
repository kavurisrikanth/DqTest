package classes;

import rest.GraphQLInputContext;

public class GroupedTransactionsRequest implements rest.IGraphQLInput {
  public double amount;

  public GroupedTransactionsRequest() {}

  public GroupedTransactionsRequest(double amount) {
    this.amount = amount;
  }

  @Override
  public void fromInput(GraphQLInputContext ctx) {
    if (ctx.has("amount")) {
      amount = ctx.readDouble("amount");
    }
  }
}
