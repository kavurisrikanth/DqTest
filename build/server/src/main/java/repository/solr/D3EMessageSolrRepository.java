package repository.solr;

@org.springframework.stereotype.Repository
public interface D3EMessageSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<models.D3EMessage, Long> {}
