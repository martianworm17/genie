/*
 * Copyright 2015 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.genie.web.jpa.specifications;

import com.google.common.collect.Sets;
import com.netflix.genie.common.dto.ApplicationStatus;
import com.netflix.genie.web.jpa.entities.ApplicationEntity;
import com.netflix.genie.web.jpa.entities.ApplicationEntity_;
import com.netflix.genie.web.jpa.entities.TagEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.util.Set;
import java.util.UUID;

/**
 * Tests for the application specifications.
 *
 * @author tgianos
 */
public class JpaApplicationSpecsTest {

    private static final String NAME = "tez";
    private static final String USER_NAME = "tgianos";
    private static final TagEntity TAG_1 = new TagEntity("tez");
    private static final TagEntity TAG_2 = new TagEntity("yarn");
    private static final TagEntity TAG_3 = new TagEntity("hadoop");
    private static final Set<TagEntity> TAGS = Sets.newHashSet(TAG_1, TAG_2, TAG_3);
    private static final Set<ApplicationStatus> STATUSES
        = Sets.newHashSet(ApplicationStatus.ACTIVE, ApplicationStatus.DEPRECATED);
    private static final String TYPE = UUID.randomUUID().toString();

    private Root<ApplicationEntity> root;
    private CriteriaQuery<?> cq;
    private CriteriaBuilder cb;
    private SetJoin<ApplicationEntity, TagEntity> tagEntityJoin;

    /**
     * Setup some variables.
     */
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        this.root = (Root<ApplicationEntity>) Mockito.mock(Root.class);
        this.cq = Mockito.mock(CriteriaQuery.class);
        this.cb = Mockito.mock(CriteriaBuilder.class);

        final Path<Long> idPath = (Path<Long>) Mockito.mock(Path.class);
        Mockito.when(this.root.get(ApplicationEntity_.id)).thenReturn(idPath);

        final Path<String> namePath = (Path<String>) Mockito.mock(Path.class);
        final Predicate equalNamePredicate = Mockito.mock(Predicate.class);
        final Predicate likeNamePredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.root.get(ApplicationEntity_.name)).thenReturn(namePath);
        Mockito.when(this.cb.equal(namePath, NAME)).thenReturn(equalNamePredicate);
        Mockito.when(this.cb.like(namePath, NAME)).thenReturn(likeNamePredicate);

        final Path<String> userNamePath = (Path<String>) Mockito.mock(Path.class);
        final Predicate equalUserNamePredicate = Mockito.mock(Predicate.class);
        final Predicate likeUserNamePredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.root.get(ApplicationEntity_.user)).thenReturn(userNamePath);
        Mockito.when(this.cb.equal(userNamePath, USER_NAME)).thenReturn(equalUserNamePredicate);
        Mockito.when(this.cb.like(userNamePath, USER_NAME)).thenReturn(likeUserNamePredicate);

        final Path<ApplicationStatus> statusPath = (Path<ApplicationStatus>) Mockito.mock(Path.class);
        final Predicate equalStatusPredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.root.get(ApplicationEntity_.status)).thenReturn(statusPath);
        Mockito.when(this.cb.equal(Mockito.eq(statusPath), Mockito.any(ApplicationStatus.class)))
            .thenReturn(equalStatusPredicate);

        this.tagEntityJoin = (SetJoin<ApplicationEntity, TagEntity>) Mockito.mock(SetJoin.class);
        Mockito.when(this.root.join(ApplicationEntity_.tags)).thenReturn(this.tagEntityJoin);
        final Predicate tagInPredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.tagEntityJoin.in(TAGS)).thenReturn(tagInPredicate);

        final Expression<Long> idCountExpression = (Expression<Long>) Mockito.mock(Expression.class);
        Mockito.when(this.cb.count(Mockito.any())).thenReturn(idCountExpression);
        final Predicate havingPredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.cb.equal(idCountExpression, TAGS.size())).thenReturn(havingPredicate);

        final Path<String> typePath = (Path<String>) Mockito.mock(Path.class);
        final Predicate equalTypePredicate = Mockito.mock(Predicate.class);
        final Predicate likeTypePredicate = Mockito.mock(Predicate.class);
        Mockito.when(this.root.get(ApplicationEntity_.type)).thenReturn(typePath);
        Mockito.when(this.cb.equal(typePath, TYPE)).thenReturn(equalTypePredicate);
        Mockito.when(this.cb.like(typePath, TYPE)).thenReturn(likeTypePredicate);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindAll() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(NAME, USER_NAME, STATUSES, TAGS, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }

        // Tags
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));

        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindAllLike() {
        final String newName = NAME + "%";
        final String newUser = USER_NAME + "%";
        final String newType = TYPE + "%";
        final Specification<ApplicationEntity> spec
            = JpaApplicationSpecs.find(newName, newUser, STATUSES, TAGS, newType);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).like(this.root.get(ApplicationEntity_.name), newName);
        Mockito.verify(this.cb, Mockito.times(1)).like(this.root.get(ApplicationEntity_.user), newUser);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.times(1)).like(this.root.get(ApplicationEntity_.type), newType);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindNoName() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(null, USER_NAME, STATUSES, TAGS, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.never()).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindNoUserName() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(NAME, null, STATUSES, TAGS, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.never()).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindNoStatuses() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(NAME, USER_NAME, null, TAGS, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.never()).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindEmptyStatuses() {
        final Specification<ApplicationEntity> spec
            = JpaApplicationSpecs.find(NAME, USER_NAME, Sets.newHashSet(), TAGS, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.never()).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindNoTags() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(NAME, USER_NAME, STATUSES, null, TYPE);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.never()).join(ApplicationEntity_.tags);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }

    /**
     * Test the find specification.
     */
    @Test
    public void testFindNoType() {
        final Specification<ApplicationEntity> spec = JpaApplicationSpecs.find(NAME, USER_NAME, STATUSES, TAGS, null);

        spec.toPredicate(this.root, this.cq, this.cb);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.name), NAME);
        Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.user), USER_NAME);
        for (final ApplicationStatus status : STATUSES) {
            Mockito.verify(this.cb, Mockito.times(1)).equal(this.root.get(ApplicationEntity_.status), status);
        }
        Mockito.verify(this.root, Mockito.times(1)).join(ApplicationEntity_.tags);
        Mockito.verify(this.tagEntityJoin, Mockito.times(1)).in(TAGS);
        Mockito.verify(this.cq, Mockito.times(1)).groupBy(Mockito.any(Path.class));
        Mockito.verify(this.cq, Mockito.times(1)).having(Mockito.any(Predicate.class));
        Mockito.verify(this.cb, Mockito.never()).equal(this.root.get(ApplicationEntity_.type), TYPE);
    }
}
