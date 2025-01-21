package tasks

import contributors.*
import retrofit2.Response

fun loadContributorsBlocking(service: GitHubService, req: RequestData) : List<User> {
    // 1. 주어진 organization 하위의 리포지토리를 불러옵니다.
    val repos = service
        .getOrgReposCall(req.org)   // #1: *Call 클래스의 인스턴스를 반환 합니다. 아직 요청은 수행되기 전 입니다.
        .execute()                  // #2: 요청을 수행하며 현재 스레드를 블락합니다.
        .also { logRepos(req, it) } // #3: 로그를 남깁니다.
        .body() ?: emptyList()      // #4: response 의 body 로 부터 결과를 얻습니다.

    // 2. 리포지토리를 순회하며 컨트리뷰터들을 불러오고, 결과를 병합하여 리턴합니다.
    return repos.flatMap { repo ->
        service
            .getRepoContributorsCall(req.org, repo.name)    // #1: *Call 클래스의 인스턴스를 반환 합니다. 아직 요청은 수행되기 전 입니다.
            .execute()                                      // #2: 요청을 수행하며 현재 스레드를 블락합니다.
            .also { logUsers(repo, it) }                    // #3: 로그를 남깁니다.
            .bodyList()                                     // #4: response 의 body 로 부터 결과를 얻습니다.
    }.aggregate() // #5: 결과를 병합 합니다. List<List<User>> -> List<User>. 동일한 유저는 contributions(기여 수)가 합산되며 합쳐집니다.
}

fun <T> Response<List<T>>.bodyList(): List<T> {
    return body() ?: emptyList()
}