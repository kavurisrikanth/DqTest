package repository.solr;

@org.springframework.stereotype.Repository
public interface EmailMessageSolrRepository
    extends org.springframework.data.solr.repository.SolrCrudRepository<
        models.EmailMessage, Long> {}
