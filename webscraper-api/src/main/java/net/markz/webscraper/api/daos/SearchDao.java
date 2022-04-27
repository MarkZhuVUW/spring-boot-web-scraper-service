package net.markz.webscraper.api.daos;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SearchDao implements ISearchDao {}
