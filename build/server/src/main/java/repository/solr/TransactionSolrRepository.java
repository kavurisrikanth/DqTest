package repository.solr;

@org.springframework.stereotype.Repository
public interface TransactionSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.Transaction, Long> {}
