package tasks

import contributors.GitHubService
import contributors.Repo
import contributors.RequestData
import contributors.User
import contributors.logRepos
import contributors.logUsers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos: List<Repo> = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    return@coroutineScope repos.map { repo ->
        async {
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .body() ?: emptyList()
        }
    }
        .awaitAll()
        .flatten()
        .aggregate()
}
