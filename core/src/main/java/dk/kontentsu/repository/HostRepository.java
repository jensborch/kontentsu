/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import dk.kontentsu.model.Host;
import dk.kontentsu.model.Item;

/**
 * Repository for performing CRUD operations on host objects.
 *
 * @author Jens Borch Christiansen
 */
@ApplicationScoped
@Transactional(Transactional.TxType.MANDATORY)
public class HostRepository extends Repository<Host> {

    @Inject
    ItemRepository itemRepo;

    @Override
    public List<Host> findAll() {
        TypedQuery<Host> query = em.createNamedQuery(HOST_FIND_ALL, Host.class);
        return query.getResultList();
    }

    @Override
    public Host get(final UUID uuid) {
        TypedQuery<Host> query = em.createNamedQuery(HOST_GET, Host.class);
        query.setParameter("uuid", uuid);
        return query.getSingleResult();
    }

    public Host getByName(final String name) {
        TypedQuery<Host> query = em.createNamedQuery(HOST_GET_BY_NAME, Host.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public Optional<Host> findByName(final String name) {
        try {
            return Optional.of(getByName(name));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(final UUID uuid) {
        Host h = get(uuid);
        removeHostFromItem(h);
        em.remove(h);
    }

    public void delete(final String name) {
        Host h = getByName(name);
        removeHostFromItem(h);
        em.remove(h);
    }

    private void removeHostFromItem(final Host host) {
        itemRepo.find(Item.Criteria.create().host(host.getName())).forEach(i -> i.removeHost(host));
    }

}
