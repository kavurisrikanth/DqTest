package repository.solr;

@org.springframework.stereotype.Repository
public interface CustomerSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.Customer, Long> {}
