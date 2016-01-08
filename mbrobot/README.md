### English:

# Mercado Bitcoin Java Client for API and Trade API

This project is a client for Java developers to use Mercado Bitcoin Market Data API and Trade API. It uses a thrid party Json lib embedded to avoid lib dependencies. It is developed under proposed Maven file structure, using Checkstyle and PDM to enhance code quality. It is under MIT License (see **LICENSE.txt**).


### Responsabilities:

We are not responsible for any bug, misuse or malfunction of this source code, use it at your own risk.


### Contents:
 - **Binaries (JARs):** available as compiled, sources and javadoc at folder ``target``;
 - **API methods:** class ``net.mercadobitcoin.tradeapi.service.ApiService``: provide access to Mercado Bitcoin public API. It allows to retrieve information about orders and trades [read more > API](https://www.mercadobitcoin.net/api/);
 - **Trade API methods:** class ``net.mercadobitcoin.tradeapi.service.TradeApiService``: provide access to Mercado Bitcoin Trade API, then requires API keys (generated at [Configurações > Trade API (chaves)](https://www.mercadobitcoin.net/tapi/configuracoes/)). It allow to: retrieve user's info; create buy and sell orders; cancel orders; list user's orders; withdrawal Bitcoins; [read more > Trade API](https://www.mercadobitcoin.net/trade-api/);
 - **Usage example and extra tests:** package ``net.mercadobitcoin.tradeapi.test``. For Trade API is necessary to fill constant values at class ``net.mercadobitcoin.tradeapi.test.base.UserInfo``; Tests use Junit 4;
 - **Data flow:** uses classes under package ``net.mercadobitcoin.tradeapi.to``;
 - **Thrid party:** package ``com.eclipsesource.json`` contains thirdy party lib to convert json data. 


### Import in Eclipse/STS:

This project can be easily imported in Eclipse using Maven plugin (or instead of Eclipse, using STS, its version tuned by SpringSource Inc. - Pivotal Software, Inc.) using option **Import... > Maven > Existing Maven Projects**.


### Support and issues:

Please, to ask for support or report any found issues, write to support@mercadobitcoin.net.



<br/><br/>

### Português:

# Mercado Bitcoin cliente Java para API e API de Negociações

Esse projeto é um cliente para desenvolvedores Java utilizarem a API de Dados de Mercado e API de Negociações do Mercado Bitcoin. It uses uma biblioteca de terceiros para conversão de Json, empacotada juntamente para evitar dependência de biblioteca. Foi desenvolvido na estrutura de arquivos proposta pelo Maven, utilizando também Checkstyle e PDM para aumentar a qualidade do código. Está sob a licença MIT (ver **LICENSE.txt**).


### Responsabilidades:

Nós não nos responsabilizamos por qualquer problema, mal uso ou mal funcionamento desse código fonte, utilize a seu próprio risco.


### Conteúdo:
 - **Binários (JARs):** disponíveis como compilado, código fonte e javadoc na pasta ``target``;
 - **Métodos da API:** classe ``net.mercadobitcoin.tradeapi.service.ApiService``: fornece accesso à API pública do Mercado Bitcoin. Permite recuperar informações sobre ordens e negociações [leia mais > API](https://www.mercadobitcoin.net/api/);
 - **Métodos da API de Negociações:** classe ``net.mercadobitcoin.tradeapi.service.TradeApiService``: fornece accesso à API de Negociações do Mercado Bitcoin, logo requer chaves de API (geradas em [Configurações > Trade API (chaves)](https://www.mercadobitcoin.net/tapi/configuracoes/)). Permite: ler informações do usuário; criar ordens de venda e compra; cancelar ordens; listar ordens do usuário; transferir Bitcoins; [leia mais > Trade API](https://www.mercadobitcoin.net/trade-api/);
 - **Exemplos de uso e testes extras:** pacote ``net.mercadobitcoin.tradeapi.test``. Para API de Negociações é necessário preencher as constantes da classe ``net.mercadobitcoin.tradeapi.test.base.UserInfo``; Testes utilizam Junit 4;
 - **Fluxo de dados:** utiliza classes do pacote ``net.mercadobitcoin.tradeapi.to``;
 - **Terceiros:** pacote ``com.eclipsesource.json`` contém biblioteca de terceiros para converter dados Json. 


### Importar no Eclipse/STS:

Esse projeto pode ser facilmente importado no Eclipse utlizando o plugin do Maven (ou ao invés do Eclipse, utiliando STS, versão melhorada da SpringSource Inc. - Pivotal Software, Inc.) na opção  **Import... > Maven > Existing Maven Projects**.


### Suporte e problemas:

Por favor, para requisitar suporte or reportar algum problema encontrado, escreva para suporte@mercadobitcoin.net.